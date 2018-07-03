package cn.suniper.mesh.loadbalance.client.util;

import cn.suniper.mesh.loadbalance.client.tcp.NettyClientProperties;
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
        properties.put("groupEventType", "io.netty.channel.nio.NioEventLoopGroup");
        properties.put("socketChannelType", "io.netty.channel.socket.nio.NioSocketChannel");
        properties.put("workers", 4);
        properties.put("maxPoolConn", 30);
        properties.put("channelPipelines", "a,b,c");

        NettyClientProperties clientProperties = PropertiesUtil.getClientPropFromProperties(properties);
        assertEquals(properties.get("groupEventType"), clientProperties.getGroupEventType());
        assertEquals(properties.get("socketChannelType"), clientProperties.getSocketChannelType());
        assertEquals(properties.get("workers"), clientProperties.getWorkers());
        assertEquals(properties.get("maxPoolConn"), clientProperties.getMaxPoolConn());
        assertEquals(properties.getProperty("channelPipelines"), String.join(",", clientProperties.getChannelPipelines()));

    }

}