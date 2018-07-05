package cn.suniper.mesh.transport.tcp;

import cn.suniper.mesh.transport.TransportConfigKey;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public class ConnectionPoolManagerTest {

    @Test
    public void initFromClientProperties() throws Exception {
        List<String> channels = Arrays.asList(
                HttpClientCodec.class.getName(),
                HttpContentDecompressor.class.getName()
        );

        Properties properties = new Properties();
        properties.put(TransportConfigKey.GROUP_EVENT_TYPE.propName(), "io.netty.channel.nio.NioEventLoopGroup");
        properties.put(TransportConfigKey.SOCKET_CHANNEL_TYPE.propName(), "io.netty.channel.socket.nio.NioSocketChannel");
        properties.put(TransportConfigKey.WORKERS.propName(), 4);
        properties.put(TransportConfigKey.MAX_POOL_CONN.propName(), 30);
        properties.put(TransportConfigKey.CHANNEL_PIPELINES.propName(), String.join(",", channels));

        ConnectionPoolManager manager = ConnectionPoolManager.initFromClientProperties(properties);
        assertEquals(properties.get(TransportConfigKey.SOCKET_CHANNEL_TYPE.propName()), manager.getSocketChannelType().getName());
        assertEquals(properties.get(TransportConfigKey.GROUP_EVENT_TYPE.propName()), manager.getGroupType().getName());
        manager.shutdown();
    }

}