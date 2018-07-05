package cn.suniper.mesh.discovery.commons;

import cn.suniper.mesh.discovery.KVStore;
import cn.suniper.mesh.discovery.model.Application;
import com.coreos.jetcd.Client;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * 根据包引入的情况自动初始化相应的KvStore实例
 *
 * @author Rao Mengnan
 *         on 2018/5/11.
 */
public class ConnAutoInitializer {

    private static final int DEFAULT_TIMEOUT = 1000;

    /**
     * 通过环境现有依赖自动判断服务注册的框架
     * @param storeSource 连接服务注册的客户端
     * @return KVStore
     * @throws ClassNotFoundException 没有相关的依赖
     */
    public static KVStore getStore(Object storeSource) throws ClassNotFoundException {
        KvSource.Provider provider = KvSource.getSourceType();
        return getStore(provider, storeSource);
    }

    /**
     * 通过环境现有依赖自动判断服务注册的框架，并通过配置中的registryUrls自动创建相应的客户端
     * @param application 连接服务注册的客户端
     * @return KVStore
     * @throws ClassNotFoundException 没有相关的依赖
     */
    public static KVStore getStore(Application application) throws ClassNotFoundException, IOException, InterruptedException {
        KvSource.Provider provider = KvSource.getSourceType();
        Object storeSource = null;
        switch (provider) {
            case ETCD: {
                if (application != null) {
                    storeSource = etcdConnection(application.getRegistryUrlList());
                }
                break;
            }
            case ZOOKEEPER: {
                if (application != null) {
                    String hosts = String.join(",", application.getRegistryUrlList());
                    storeSource = zkConnection(hosts, DEFAULT_TIMEOUT);
                }
                break;
            }
            default:

        }
        return getStore(provider, storeSource);
    }

    /**
     * 根据服务注册应用的类型创建KVStore
     * @param provider 连接服务注册的客户端
     * @param storeSource 连接服务注册的客户端
     * @return KVStore
     */
    public static KVStore getStore(KvSource.Provider provider, Object storeSource) {
        Constructor<? extends KVStore> constructor;

        switch (provider) {
            case ETCD: {

                Class<? extends KVStore> type = provider.getType();
                try {
                    assert type != null;
                    constructor = type.getConstructor(Client.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case ZOOKEEPER: {
                Class<? extends KVStore> type = provider.getType();
                try {
                    assert type != null;
                    constructor = type.getConstructor(Client.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            default:
                throw new RuntimeException("Unsupported store type");
        }

        try {
            return constructor.newInstance(storeSource);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Store source mismatch", e);
        }
    }


    public static ZooKeeper zkConnection(String hosts, int timeout) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(hosts, timeout, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                latch.countDown();
            }
        });
        latch.await();
        return zooKeeper;
    }

    public static Client etcdConnection(Collection<String> endpoints) {
        return Client.builder()
                .endpoints(endpoints)
                .build();
    }
}
