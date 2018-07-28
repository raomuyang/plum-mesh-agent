package cn.suniper.mesh.transport.tcp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class NettyClientPropertiesTest {
    @Test
    public void testEquals() throws Exception {
        NettyClientProperties properties1 = new NettyClientProperties();
        properties1.setMaxPoolConn(1);

        NettyClientProperties properties2 = new NettyClientProperties();
        properties2.setMaxPoolConn(1);

        assertTrue(properties1.equals(properties2));
    }


    @Test
    public void testHashCode() throws Exception {
        NettyClientProperties properties1 = new NettyClientProperties();
        properties1.setMaxPoolConn(1);

        NettyClientProperties properties2 = new NettyClientProperties();
        properties2.setMaxPoolConn(1);

        assertEquals(properties1.hashCode(), properties2.hashCode());
    }

}