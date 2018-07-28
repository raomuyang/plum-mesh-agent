package cn.suniper.mesh.discovery.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class ProviderInfoTest {
    @Test
    public void equals() throws Exception {
        ProviderInfo info1 = new ProviderInfo();
        info1.setIp("0.0.0.0");
        info1.setVersion(1);
        info1.setPort(80);
        info1.setName("test");
        info1.setWeight(1);

        ProviderInfo info2 = new ProviderInfo();
        info2.setIp("0.0.0.0");
        info2.setVersion(1);
        info2.setPort(80);
        info2.setName("test");
        info2.setWeight(1);

        assertEquals(info1.hashCode(), info2.hashCode());
        assertTrue(info1.equals(info2));
        info1.setVersion(2);
        assertFalse(info1.equals(info2));
    }

    @Test
    public void testCreate() {
        ProviderInfo info1 = new ProviderInfo("name", "0.0.0.0", 1, 22, 2);
        ProviderInfo info2 = new ProviderInfo("name", "0.0.0.0", 22);
        assertEquals(info1.getIp(), info2.getIp());
        assertEquals(info1.getPort(), info2.getPort());
        assertNotEquals(info1.getWeight(), info2.getWeight());
        assertNotEquals(info1.getVersion(), info2.getVersion());
    }
}