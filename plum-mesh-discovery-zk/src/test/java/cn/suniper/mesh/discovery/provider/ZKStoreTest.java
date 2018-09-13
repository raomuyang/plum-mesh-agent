package cn.suniper.mesh.discovery.provider;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 * on 2018/9/13.
 */
public class ZKStoreTest {

    @Test
    public void testGetParentNameSuccessfully() {
        assertEquals("/", ZKStore.getParentName("/"));
        assertEquals("/test/folder", ZKStore.getParentName("/test/folder/name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetParentNameFailed() {
        ZKStore.getParentName("/test/folder//name");
        fail();
    }

}