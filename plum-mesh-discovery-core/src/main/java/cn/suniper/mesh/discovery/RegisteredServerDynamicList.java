package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import cn.suniper.mesh.discovery.util.MapperUtil;
import com.google.common.collect.Lists;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListUpdater;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The dynamic list created by {@link KVStore} and {@link Application#serverGroup},
 * {@link Application#serverGroup} will be used to find the root node of the
 * service list, and then get all available child nodes from {@link KVStore}
 * <p>
 * 通过{@link KVStore}和{@link Application#serverGroup}创建的动态列表，
 * 会使用{@link Application#serverGroup}找到服务列表的根节点，
 * 然后从{@link KVStore}中获取所有可用的子节点
 * <p>
 * You can get the {@link ServerListUpdater} bound to {@link KVStore}
 * through {@link RegisteredServerDynamicList#getKvStoreBasedUpdater()},
 * which can be used to update the {@link ServerList}.
 * <p>
 * 你可以通{@link RegisteredServerDynamicList#getKvStoreBasedUpdater()}获取绑定了{@link KVStore} 的{@link ServerListUpdater}，
 * 可用于更新{@link ServerList}
 *
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class RegisteredServerDynamicList implements ServerList<RegisteredServer> {

    private Log log = LogFactory.getLog(getClass());

    private KVStore store;
    private String parentNode;
    private String serverGroup;
    private Map<String, ProviderInfo> providerMap;

    public RegisteredServerDynamicList(KVStore store, Application application) {
        this(store, Optional.ofNullable(application)
                .map(Application::getServerGroup)
                .orElse(null));
    }

    public RegisteredServerDynamicList(KVStore store, String serverGroup) {
        if (serverGroup == null) throw new IllegalArgumentException("serverGroup must be not null");
        this.store = store;
        this.serverGroup = serverGroup;

        this.providerMap = new ConcurrentHashMap<>();
        this.parentNode = String.join("/", Constants.STORE_ROOT, this.serverGroup);
    }


    /**
     * Get the {@link ServerListUpdater}  bound to KvStore, which can be used to update the {@link KVStore}
     * <p>
     * 获取绑定了{@link ServerListUpdater} 的ServerListUpdater，可用于更新{@link KVStore}
     *
     * @return {@link RegistryServerListUpdater}
     */
    public ServerListUpdater getKvStoreBasedUpdater() {
        return new RegistryServerListUpdater(store, parentNode, providerMap);
    }

    /**
     * Get the {@link ServerListUpdater}  bound to KvStore, which can be used to update the {@link KVStore}
     * <p>
     * 获取绑定了{@link ServerListUpdater} 的ServerListUpdater，可用于更新{@link KVStore}
     *
     * @param executorService Specify a custom ExecutorService, otherwise it will be executed in the {@link java.util.concurrent.ThreadPoolExecutor} by default。
     *                        指定自定义的ExecutorService，否则的话会在默认的{@link java.util.concurrent.ThreadPoolExecutor}中执行
     * @return {@link RegistryServerListUpdater}
     */
    public ServerListUpdater getKvStoreBasedUpdater(ExecutorService executorService) {
        return new RegistryServerListUpdater(store, parentNode, providerMap)
                .setExecutorService(executorService);
    }

    /**
     * Get cached Server list, won't get updates from kvStore
     * <p>
     * 获取缓存的Server list，不会从kvStore中获取更新
     *
     * @return 本地缓存的记录
     */
    public Collection<ProviderInfo> getCachedListOfServers() {
        return providerMap.values();
    }

    /**
     * Get the initial Server list from kvStore, update the local cache
     * <p>
     * 从kvStore中获取初始的Server list，更新本地缓存
     *
     * @return 最新的服务列表
     */
    @Override
    public List<RegisteredServer> getInitialListOfServers() {
        return obtainServerListByKvStore();
    }

    /**
     * Get the updated Server list from kvStore and update the local cache
     * <p>
     * 从kvStore中获取更新后的Server list，并更新本地缓存
     *
     * @return 最新的服务列表
     */
    @Override
    public List<RegisteredServer> getUpdatedListOfServers() {
        return obtainServerListByKvStore();
    }

    List<RegisteredServer> obtainServerListByKvStore() {
        Set<String> newKeys = new HashSet<>();
        Consumer<Node> collectNewNodesToSet = node -> newKeys.add(node.getKey());

        try {
            List<Node> nodeInfoList = store.list(parentNode);
            Stream<ProviderInfo> stream = nodeInfoList
                    .stream()
                    .peek(collectNewNodesToSet)
                    .map(node -> providerMap.computeIfAbsent(
                            node.getKey(), k -> MapperUtil.node2Provider(node)));

            providerMap.keySet()
                    .parallelStream()
                    .filter(k -> !newKeys.contains(k))
                    .forEach(k -> providerMap.remove(k));
            return map2ServerList(stream);

        } catch (Exception e) {
            log.warn(String.format("failed to obtain list of servers: %s", parentNode), e);
            return Lists.newArrayList();
        }
    }

    Map<String, ProviderInfo> getProviderMap() {
        return providerMap;
    }

    private List<RegisteredServer> map2ServerList(Stream<ProviderInfo> stream) {
        return stream
                .filter(Objects::nonNull)
                .map(i -> new RegisteredServer(serverGroup, i))
                .collect(Collectors.toList());
    }
}
