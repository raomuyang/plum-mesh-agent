package cn.suniper.mesh.discovery.commons;

import cn.suniper.mesh.discovery.provider.EtcdStore;
import cn.suniper.mesh.discovery.provider.ZKStore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
public class KvSourceTest {

    @Test
    public void testGetSource() {
        assertTrue(KvSource.Provider.ETCD.isPresent());
        assertEquals(EtcdStore.class, KvSource.Provider.ETCD.getType());
        assertTrue(KvSource.Provider.ETCD.isPresent());
        assertEquals(EtcdStore.class, KvSource.Provider.ETCD.getType());

        assertTrue(KvSource.Provider.ZOOKEEPER.isPresent());
        assertEquals(ZKStore.class, KvSource.Provider.ZOOKEEPER.getType());
        assertTrue(KvSource.Provider.ZOOKEEPER.isPresent());
        assertEquals(ZKStore.class, KvSource.Provider.ZOOKEEPER.getType());

        assertTrue(KvSource.Provider.AUTO.isPresent());
        assertEquals(EtcdStore.class, KvSource.Provider.AUTO.getType());
        assertTrue(KvSource.Provider.AUTO.isPresent());
        assertEquals(EtcdStore.class, KvSource.Provider.AUTO.getType());
    }

}