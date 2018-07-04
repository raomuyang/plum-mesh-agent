package cn.suniper.mesh.transport.tcp;

import cn.suniper.mesh.transport.util.PropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * @author Rao Mengnan
 *         on 2018/6/22.
 */
public class ConnectionPoolManager {

    private static Log log = LogFactory.getLog(ConnectionPoolManager.class);

    private static final Class<? extends EventLoopGroup> DEFAULT_GROUP_TYPE = NioEventLoopGroup.class;
    private static final Class DEFAULT_CHANNEL = NioSocketChannel.class;

    private static final int DEFAULT_MAX_CONNECTIONS = 20;
    private static final int DEFAULT_WORKER_THREAD = 4;

    private int maxConn;
    private int nThread;
    private Bootstrap bootstrap;
    private Class<? extends Channel> socketChannelType;
    private Class<? extends EventLoopGroup> groupType;
    private ChannelPoolHandler channelPoolHandler;

    private ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;
    private EventLoopGroup loopGroup;


    public static class Builder {
        private ConnectionPoolManager poolManager;

        public Builder(Bootstrap bootstrap) {
            this.poolManager = new ConnectionPoolManager();
            this.poolManager.bootstrap = bootstrap;
        }

        public Builder setGroupType(Class<? extends EventLoopGroup> groupType) {
            poolManager.groupType = groupType;
            return this;
        }

        public Builder setSocketChannelType(Class<? extends Channel> socketChannelType) {
            poolManager.socketChannelType = socketChannelType;
            return this;
        }

        public Builder setMaxConn(int maxConn) {
            poolManager.maxConn = maxConn;
            return this;
        }

        public Builder setWorkerThread(int nThread) {
            poolManager.nThread = nThread;
            return this;
        }

        public Builder setChannelPoolHandler(ChannelPoolHandler channelPoolHandler) {
            poolManager.channelPoolHandler = channelPoolHandler;
            return this;
        }

        public ConnectionPoolManager build() {
            poolManager.init();
            return poolManager;
        }


    }

    private void init() {
        this.groupType = this.groupType == null ? DEFAULT_GROUP_TYPE : this.groupType;
        this.socketChannelType = socketChannelType == null ? DEFAULT_CHANNEL : socketChannelType;
        this.nThread = nThread > 0 ? nThread : DEFAULT_WORKER_THREAD;
        this.maxConn = maxConn > 0 ? maxConn : DEFAULT_MAX_CONNECTIONS;

        try {
            loopGroup = this.groupType.getConstructor(int.class).newInstance(nThread);
        } catch (InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | InvocationTargetException e) {
            throw new IllegalArgumentException("No such event group type", e);
        }

        bootstrap.group(loopGroup)
                .channel(socketChannelType);


        this.poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(bootstrap.remoteAddress(key), channelPoolHandler, maxConn);
            }
        };
    }

    public FixedChannelPool getChannelPool(InetSocketAddress address) {
        if (poolMap == null) return null;
        return poolMap.get(address);
    }

    public void shutdown() {
        if (loopGroup != null) loopGroup.shutdownGracefully();
    }

    public int getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public int getnThread() {
        return nThread;
    }

    public void setnThread(int nThread) {
        this.nThread = nThread;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Class<? extends Channel> getSocketChannelType() {
        return socketChannelType;
    }

    public void setSocketChannelType(Class<? extends Channel> socketChannelType) {
        this.socketChannelType = socketChannelType;
    }

    public Class<? extends EventLoopGroup> getGroupType() {
        return groupType;
    }

    public void setGroupType(Class<? extends EventLoopGroup> groupType) {
        this.groupType = groupType;
    }

    public ChannelPoolHandler getChannelPoolHandler() {
        return channelPoolHandler;
    }

    public void setChannelPoolHandler(ChannelPoolHandler channelPoolHandler) {
        this.channelPoolHandler = channelPoolHandler;
    }

    public static ConnectionPoolManager initFromClientProperties(Properties properties) throws ClassNotFoundException {
        NettyClientProperties tcpClientProperties = PropertiesUtil.getClientPropFromProperties(properties);
        return initFromClientProperties(tcpClientProperties);

    }

    @SuppressWarnings("unchecked")
    public static ConnectionPoolManager initFromClientProperties(NettyClientProperties properties, Initializer initializer) throws ClassNotFoundException {
        if (initializer == null) throw new IllegalArgumentException("Pipeline initializer must be not null");
        Class groupEventType = null;
        if (properties.getGroupEventType() != null) {
            groupEventType = Class.forName(properties.getGroupEventType());
        }

        Class socketChannelType = null;
        if (properties.getSocketChannelType() != null) {
            socketChannelType = Class.forName(properties.getSocketChannelType());
        }

        Bootstrap bootstrap = new Bootstrap()
                .option(ChannelOption.SO_KEEPALIVE, true);
        return new ConnectionPoolManager
                .Builder(bootstrap)
                .setWorkerThread(properties.getWorkers())
                .setMaxConn(properties.getMaxPoolConn())
                .setGroupType(groupEventType)
                .setSocketChannelType(socketChannelType)
                .setChannelPoolHandler(new DefaultChannelPoolHandler(initializer))
                .build();
    }

    public static ConnectionPoolManager initFromClientProperties(NettyClientProperties properties) throws ClassNotFoundException {
        DefaultPipelineInitializer pipelineInitializer = new DefaultPipelineInitializer(properties.getChannelPipelines());
        return initFromClientProperties(properties, pipelineInitializer);
    }

    private static class DefaultChannelPoolHandler implements ChannelPoolHandler {

        private Initializer initializer;

        DefaultChannelPoolHandler(Initializer initializer) {
            this.initializer = initializer;
        }

        @Override
        public void channelReleased(Channel ch) throws Exception {
            log.info(String.format("channel released, is open: %s, is active: %s, is registered: %s, is writable: %s",
                    ch.isOpen(), ch.isActive(),
                    ch.isRegistered(), ch.isWritable()));
        }

        @Override
        public void channelAcquired(Channel ch) throws Exception {
            log.info(String.format("channel acquired, is open: %s, is active: %s, is registered: %s, is writable: %s",
                    ch.isOpen(), ch.isActive(),
                    ch.isRegistered(), ch.isWritable()));
        }

        @Override
        public void channelCreated(Channel ch) throws Exception {
            log.info(String.format("channel created, is open: %s, is active: %s, is registered: %s, is writable: %s",
                    ch.isOpen(), ch.isActive(),
                    ch.isRegistered(), ch.isWritable()));

            initializer.initChannel(ch);
        }
    }

}
