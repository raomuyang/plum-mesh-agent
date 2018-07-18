package cn.suniper.mesh.discovery.util;

import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/18.
 */
public class MapperUtilTest {
    @Test
    public void testNode2Provider() throws Exception {
        Node node =  new Node("node1", "ip/8080/1", 0);
        ProviderInfo providerInfo = MapperUtil.node2Provider(node);
        assertNotNull(providerInfo);
        assertEquals("ip", providerInfo.getIp());
        assertEquals(8080, providerInfo.getPort());
        assertEquals(1, providerInfo.getWeight());
        assertEquals("node1", providerInfo.getName());

        node =  new Node("node1", "ip/8081/2", 1);
        MapperUtil.node2Provider(node, providerInfo);
        assertEquals("ip", providerInfo.getIp());
        assertEquals(8081, providerInfo.getPort());
        assertEquals(2, providerInfo.getWeight());
        assertEquals("node1", providerInfo.getName());
    }

    @Test
    public void testNode2ProviderWithoutWeight() {
        Node node =  new Node("node1", "ip/8080", 0);
        ProviderInfo providerInfo = MapperUtil.node2Provider(node);
        assertNotNull(providerInfo);
        assertEquals("ip", providerInfo.getIp());
        assertEquals(8080, providerInfo.getPort());
        assertEquals(0, providerInfo.getWeight());
        assertEquals("node1", providerInfo.getName());
    }

}