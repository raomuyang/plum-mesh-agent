package cn.suniper.mesh.transport.tcp;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.RequestSpecificRetryHandler;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey;
import com.netflix.loadbalancer.ILoadBalancer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Rao Mengnan
 *         on 2018/6/21.
 */
public class AsyncLoadBalancingTcpClient extends AbstractLoadBalancerAwareClient<TcpRequest, AsyncTcpResponse> {

    private static final int BOOTSTRAP_DEFAULT_WORKERS = 2;
    private static final long DEFAULT_TIMEOUT = 5000;

    private Log log = LogFactory.getLog(getClass());

    private IClientConfig icc;
    private ConnectionPoolManager poolManager;
    private Bootstrap bootstrap;

    private long connectionTimeout = DEFAULT_TIMEOUT;

    public AsyncLoadBalancingTcpClient() {
        this(null);
    }

    public AsyncLoadBalancingTcpClient(ILoadBalancer lb) {
        this(lb, null, new DefaultPipelineInitializer());
    }

    public AsyncLoadBalancingTcpClient(ILoadBalancer lb, IClientConfig clientConfig, ConnectionPoolManager poolManager) {
        super(lb, clientConfig);
        init(clientConfig);

        this.poolManager = poolManager;
    }

    public AsyncLoadBalancingTcpClient(ILoadBalancer lb, IClientConfig clientConfig, Initializer initializer) {
        super(lb, clientConfig);
        init(clientConfig);

        EventLoopGroup group = new NioEventLoopGroup(BOOTSTRAP_DEFAULT_WORKERS);
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        initializer.initChannel(ch);
                    }
                });

    }

    public AsyncLoadBalancingTcpClient(ILoadBalancer lb, IClientConfig clientConfig, Bootstrap bootstrap) {
        super(lb, clientConfig);
        init(clientConfig);

        this.bootstrap = bootstrap;
    }

    private void init(IClientConfig clientConfig) {
        this.icc = clientConfig;

        if (icc != null) {
            connectionTimeout = icc.get(IClientConfigKey.Keys.ConnectTimeout);
        }
    }

    public void shutdown() {
        if (bootstrap != null) {
            EventLoopGroup group = bootstrap.config().group();
            group.shutdownGracefully();
        }
    }

    @Override
    public RequestSpecificRetryHandler getRequestSpecificRetryHandler(TcpRequest request, IClientConfig requestConfig) {

        if (!request.isRetriable()) {
            return new RequestSpecificRetryHandler(false, false, this.getRetryHandler(), requestConfig);
        }

        if (this.icc.get(CommonClientConfigKey.OkToRetryOnAllOperations, false)) {
            return new RequestSpecificRetryHandler(true, true, this.getRetryHandler(), requestConfig);
        } else {
            return new RequestSpecificRetryHandler(true, false, this.getRetryHandler(), requestConfig);
        }
    }

    @Override
    public AsyncTcpResponse execute(TcpRequest request, IClientConfig requestConfig) throws Exception {
        if (poolManager == null) {
            return executeByBootstrap(request);
        } else {
            return executeByPool(request);
        }
    }

    private AsyncTcpResponse executeByBootstrap(TcpRequest request) throws InterruptedException, ConnectTimeoutException {
        log.debug("execute request via bootstrap");
        InetSocketAddress address = new InetSocketAddress(request.getUri().getHost(),
                request.getUri().getPort());
        ChannelFuture future = bootstrap.connect(address);
        if (!future.await(connectionTimeout, TimeUnit.MILLISECONDS)) {
            throw new ConnectTimeoutException(address.toString());
        }
        Channel channel = future.channel();
        ChannelFuture writeFuture = channel.writeAndFlush(request.getData());
        return new AsyncTcpResponse(writeFuture, request.getUri());
    }

    private AsyncTcpResponse executeByPool(TcpRequest request) throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress address = new InetSocketAddress(request.getUri().getHost(),
                request.getUri().getPort());

        FixedChannelPool pool = poolManager.getChannelPool(address);
        Future<Channel> future = pool.acquire().sync();
        if (!future.isSuccess()) {
            log.debug(future.cause());
            throw new ConnectTimeoutException(String.valueOf(future.cause()));
        } else {
            Channel channel = future.get();
            ChannelFuture channelFuture;
            try {
                channelFuture = channel.writeAndFlush(request.getData());
            } catch (Throwable e) {
                pool.release(channel);
                throw new IOException(e);
            }
            return new AsyncTcpResponse(channelFuture, request.getUri(), () -> {
                pool.release(channel);
                return true;
            });
        }
    }

}
