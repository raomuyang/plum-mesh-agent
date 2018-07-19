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
 * 通过kvStore动态更新并提供可用服务的列表
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
     * 获取绑定了KvStore的ServerListUpdater，可用于更新DynamicList
     * @return {@link RegistryServerListUpdater}
     */
    public ServerListUpdater getKvStoreBasedUpdater() {
        return new RegistryServerListUpdater(store, parentNode, providerMap);
    }

    /**
     * 获取绑定了KvStore的ServerListUpdater，可用于更新DynamicList
     * @param executorService 指定自定义的ExecutorService，否则的话会在默认的{@link java.util.concurrent.ThreadPoolExecutor}中执行
     * @return {@link RegistryServerListUpdater}
     */
    public ServerListUpdater getKvStoreBasedUpdater(ExecutorService executorService) {
        return new RegistryServerListUpdater(store, parentNode, providerMap)
                .setExecutorService(executorService);
    }

    /**
     * 获取缓存的Server list，不会从kvStore中获取更新
     * @return 本地缓存的记录
     */
    public Collection<ProviderInfo> getCachedListOfServers() {
        return providerMap.values();
    }

    /**
     * 从kvStore中获取初始的Server list，更新本地缓存
     * @return 最新的服务列表
     */
    @Override
    public List<RegisteredServer> getInitialListOfServers() {
        return obtainServerListByKvStore();
    }

    /**
     * 从kvStore中获取更新后的Server list，并更新本地缓存
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
