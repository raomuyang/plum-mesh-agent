package cn.suniper.mesh.loadbalance.client.tcp;

import cn.suniper.mesh.loadbalance.client.util.PropertiesUtil;
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
        properties.put("groupEventType", "io.netty.channel.nio.NioEventLoopGroup");
        properties.put("socketChannelType", "io.netty.channel.socket.nio.NioSocketChannel");
        properties.put("workers", 4);
        properties.put("maxPoolConn", 30);
        properties.put("channelPipelines", String.join(",", channels));
        System.out.println(properties.get("channelPipelines"));

        ConnectionPoolManager manager = ConnectionPoolManager.initFromClientProperties(properties);
        assertEquals(properties.get("socketChannelType"), manager.getSocketChannelType().getName());
        assertEquals(properties.get("groupEventType"), manager.getGroupType().getName());
        manager.shutdown();
    }

}