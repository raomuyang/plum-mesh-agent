package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import com.netflix.loadbalancer.ServerListUpdater;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/18.
 */
public class RegistryServerListUpdaterTest {

    @Test
    public void testUpdater() throws InterruptedException {
        List<Node> list = Arrays.asList(
                new Node("node1", "node1/8080/0", 0),
                new Node("node2", "node2/8080/0", 0),
                new Node("node3", "node3/8080/0", 0)
        );
        TestKvStore kvStore = new TestKvStore();
        kvStore.nodeList = list;

        Map<String, ProviderInfo> providerInfoMap = new ConcurrentHashMap<>();

        RegistryServerListUpdater updater = new RegistryServerListUpdater(kvStore, "test", providerInfoMap);
        ServerListUpdater.UpdateAction action = () -> kvStore.updateCount++;
        updater.start(action);
        kvStore.childredWatchedCountDown.await();

        assertEquals(0, providerInfoMap.size());
        kvStore.consumer.accept(Event.UPDATE, list.get(0));
        kvStore.consumer.accept(Event.UPDATE, list.get(1));
        kvStore.consumer.accept(Event.UPDATE, list.get(2));
        assertEquals(3, providerInfoMap.size());

        kvStore.consumer.accept(Event.DELETE, list.get(1));
        assertEquals(2, providerInfoMap.size());
        assertTrue(providerInfoMap.keySet().contains(list.get(0).getKey()));
        assertFalse(providerInfoMap.keySet().contains(list.get(1).getKey()));

        assertFalse(kvStore.exitSignSupplier.get());
        updater.stop();
        assertTrue(kvStore.exitSignSupplier.get());


    }

    private class TestKvStore implements KVStore {
        private List<Node> nodeList;
        private Supplier<Boolean> exitSignSupplier;
        private BiConsumer<Event, Node> consumer;
        private int updateCount;
        private CountDownLatch childredWatchedCountDown = new CountDownLatch(1);

        @Override
        public Node get(String key) throws Exception {
            return null;
        }

        @Override
        public List<Node> list(String prefix) throws Exception {
            return nodeList;
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
            this.exitSignSupplier = exitSignSupplier;
            this.consumer = consumer;
            this.childredWatchedCountDown.countDown();
        }

        @Override
        public void createParentNode(String parentNode) throws Exception {

        }

        @Override
        public void close() throws Exception {

        }
    }

}