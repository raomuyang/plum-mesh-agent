package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/18.
 */
public class RegisteredServerDynamicListTest {
    @Test
    public void testObtainServerListByKvStore() throws Exception {
        List<Node> list1 = Arrays.asList(
                new Node("node1", "node1/8080/0", 0),
                new Node("node2", "node2/8080/0", 0),
                new Node("node3", "node3/8080/0", 0)
        );

        List<Node> list2 = Arrays.asList(
                new Node("node2", "node2/8080/0", 0),
                new Node("node3", "node3/8080/0", 0),
                new Node("node4", "node4/8080/0", 0)
        );

        MockKvStore store = new MockKvStore() {
            int c = 0;
            @Override
            public List<Node> list(String prefix) throws Exception {
                if (c++ % 2 == 0) return list1;
                else return list2;
            }
        };
        RegisteredServerDynamicList dynamicList = new RegisteredServerDynamicList(store, "abc");
        List<RegisteredServer> serverList1 = dynamicList.obtainServerListByKvStore();
        assertEquals(3, serverList1.size());
        for (int i = 1; i < 4; i++) {
            assertEquals("node" + i, serverList1.get(i - 1).getHost());
        }
        List<RegisteredServer> serverList2 = dynamicList.obtainServerListByKvStore();
        assertEquals(3, serverList2.size());
        for (int i = 2; i < 5; i++) {
            assertEquals("node" + i, serverList2.get(i - 2).getHost());
        }

        Map<String, ProviderInfo> map = dynamicList.getProviderMap();
        assertEquals(3, map.size());
        assertTrue(map.keySet().contains("node4"));
        assertFalse(map.keySet().contains("node1"));
    }

    private static abstract class MockKvStore implements KVStore {

        @Override
        public Node get(String key) throws Exception {
            return null;
        }

        @Override
        public List<Node> list(String prefix) throws Exception {
            return null;
        }

        @Override
        public List<String> listKeys(String prefix) throws Exception {
            return null;
        }

        @Override
        public long put(String key, String value) throws Exception {
            return 0;
        }

        @Override
        public long put(String key, String value, boolean ephemeral) throws Exception {
            return 0;
        }

        @Override
        public long delete(String key) throws Exception {
            return 0;
        }

        @Override
        public boolean exists(String key) throws Exception {
            return false;
        }

        @Override
        public void watchChildren(String key, BiConsumer<Event, Node> consumer) throws Exception {

        }

        @Override
        public void watchChildren(String key, Supplier<Boolean> exitSignSupplier, BiConsumer<Event, Node> consumer) throws Exception {

        }

        @Override
        public void createParentNode(String parentNode) throws Exception {

        }

        @Override
        public void close() throws Exception {

        }
    }

}