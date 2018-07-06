package cn.suniper.mesh.transport.util;

import cn.suniper.mesh.transport.TransportConfigKey;
import cn.suniper.mesh.transport.tcp.NettyClientProperties;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public class PropertiesUtilTest {
    @Test
    public void getClientPropFromProperties() throws Exception {
        Properties properties = new Properties();
        properties.put(TransportConfigKey.GROUP_EVENT_TYPE.propName(), "io.netty.channel.nio.NioEventLoopGroup");
        properties.put(TransportConfigKey.SOCKET_CHANNEL_TYPE.propName(), "io.netty.channel.socket.nio.NioSocketChannel");
        properties.put(TransportConfigKey.WORKERS.propName(), 4);
        properties.put(TransportConfigKey.MAX_POOL_CONN.propName(), 30);
        properties.put(TransportConfigKey.CHANNEL_PIPELINES.propName(), "a, b, c");

        NettyClientProperties clientProperties = PropertiesUtil.getClientPropFromProperties(properties);
        assertEquals(properties.get(TransportConfigKey.GROUP_EVENT_TYPE.propName()), clientProperties.getGroupEventType());
        assertEquals(properties.get(TransportConfigKey.SOCKET_CHANNEL_TYPE.propName()), clientProperties.getSocketChannelType());
        assertEquals(properties.get(TransportConfigKey.WORKERS.propName()), clientProperties.getWorkers());
        assertEquals(properties.get(TransportConfigKey.MAX_POOL_CONN.propName()), clientProperties.getMaxPoolConn());
        assertEquals(properties.getProperty(TransportConfigKey.CHANNEL_PIPELINES.propName()), String.join(", ", clientProperties.getChannelPipelines()));

    }

}