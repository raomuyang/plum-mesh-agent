package cn.suniper.mesh.discovery.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class NodeTest {
    @Test
    public void equals() throws Exception {
        Node node1 = new Node();
        node1.setKey("a");
        node1.setVersion(1);
        node1.setCreateReversion(1);
        node1.setModReversion(0);
        node1.setValue("value");

        Node node2 = new Node();
        node2.setKey("a");
        node2.setVersion(1);
        node2.setCreateReversion(1);
        node2.setModReversion(0);
        node2.setValue("value");

        assertEquals(node1.hashCode(), node2.hashCode());
        assertTrue(node1.equals(node2));

    }

    @Test
    public void testCreate() {
        Node node1 = new Node("key", "value", 1, 2, 3);
        Node node2 = new Node("key", "value", 2);
        assertEquals(node1.getModReversion(), node2.getModReversion());
        assertEquals(node1.getKey(), node2.getKey());
        assertEquals(node1.getValue(), node2.getValue());
        assertNotEquals(node1.getCreateReversion(), node2.getCreateReversion());
        assertNotEquals(node1.getVersion(), node2.getVersion());
    }

}