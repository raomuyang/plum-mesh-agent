package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import cn.suniper.mesh.discovery.util.MapperUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class ProviderDelegatingRegisterTest {
    @Test
    public void register() throws Exception {
        ProviderInfo providerInfo = new ProviderInfo("test-name", "test-ip", 22);
        assertEquals(0, providerInfo.getWeight());

        DemoKvStore store = new DemoKvStore();
        Application application = new Application(null, providerInfo, "test-group");
        ProviderDelegatingRegister register = new ProviderDelegatingRegister(store, application);
        register.register();

        String key = Constants.STORE_ROOT + "/test-group/test-name";
        Node node = store.get(key);
        ProviderInfo newProviderInfo = MapperUtil.node2Provider(node);
        assertEquals(providerInfo, newProviderInfo);

        assertTrue(store.keyEphemeral.get(key));

    }

    @Test
    public void testInitIp() throws Exception {
        ProviderInfo providerInfo = new ProviderInfo("test-name", 22);
        assertNull(providerInfo.getIp());
        Application application = new Application(null, providerInfo, "test-group");
        DemoKvStore store = new DemoKvStore();
        new ProviderDelegatingRegister(store, application).register();
        assertNotNull(providerInfo.getIp());
    }

    @Test
    public void testIllegal() throws Exception {

        ProviderInfo providerInfo = new ProviderInfo("test-name", 0);
        Application application = new Application(null, providerInfo, "test-group");
        DemoKvStore store = new DemoKvStore();
        try {
            new ProviderDelegatingRegister(store, application).register();
            fail();
        } catch (Throwable ignored) {
        }

        providerInfo.setPort(22);
        new ProviderDelegatingRegister(store, application).register();

        application.setServerGroup(null);
        try {
            new ProviderDelegatingRegister(store, application).register();
            fail();
        } catch (Throwable ignored) {
        }

    }

    class DemoKvStore implements KVStore {
        Map<String, String> kv = new HashMap<>();
        Map<String, Boolean> keyEphemeral = new HashMap<>();
        @Override
        public Node get(String key) throws Exception {
            return new Node(key, kv.get(key), 0);
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
            return put(key, value, false);
        }

        @Override
        public long put(String key, String value, boolean ephemeral) throws Exception {
            keyEphemeral.put(key, ephemeral);
            kv.put(key, value);
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