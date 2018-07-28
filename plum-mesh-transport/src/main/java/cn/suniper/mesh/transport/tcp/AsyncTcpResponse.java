package cn.suniper.mesh.transport.tcp;

import com.netflix.client.ClientException;
import com.netflix.client.IResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Netty is async client, in most cases, we handle the return value
 * in the handler. If there is a special need for ChannelFuture,
 * we can add a callback through the Response.
 *
 * @author Rao Mengnan
 *         on 2018/7/2.
 */
public class AsyncTcpResponse implements IResponse {

    private URI uri;
    private ChannelFuture channelFuture;

    private ChannelPromise promise;
    private Callable<Boolean> closeAction;

    public AsyncTcpResponse(ChannelFuture future, URI uri) {
        this(future, uri, () -> {
            if (future != null) {
                future.channel().closeFuture();
                return true;
            }
            return false;
        });
    }

    public AsyncTcpResponse(ChannelFuture future, URI uri, Callable<Boolean> closeAction) {
        if (closeAction == null) throw new IllegalArgumentException("Close action must be not null");
        this.channelFuture = future;

        this.uri = uri;
        if (future != null) {
            promise = future.channel().newPromise();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    promise.setSuccess();
                } else {
                    promise.setFailure(f.cause());
                }
            });
        }
        this.closeAction = closeAction;
    }

    @Override
    public ChannelFuture getPayload() throws ClientException {
        return channelFuture;
    }

    @Override
    public boolean hasPayload() {
        return channelFuture != null;
    }

    @Override
    public boolean isSuccess() {
        return promise != null && promise.isSuccess();
    }

    @Override
    public URI getRequestedURI() {
        return uri;
    }

    @Override
    public Map<String, ?> getHeaders() {
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            closeAction.call();
        } catch (Exception e) {
            throw new IOException("close/release channel failed", e);
        }
    }

    public Throwable getCause() {
        return promise == null ? null : promise.cause();
    }

    public void await() throws InterruptedException {
        if (promise != null) {
            promise.await();
        }
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        return promise.await(time, unit);
    }


}
