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
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class RegisteredServerDynamicList implements ServerList<RegisteredServer> {

    private Log log = LogFactory.getLog(getClass());

    private KVStore store;
    private String parentNode;
    private String appName;
    private Map<String, ProviderInfo> providerMap;

    public RegisteredServerDynamicList(KVStore store, Application application) {
        this(store, Optional.ofNullable(application)
                .map(Application::getName)
                .orElse(null));
    }

    public RegisteredServerDynamicList(KVStore store, String applicationName) {
        if (applicationName == null) throw new IllegalArgumentException("application must be not null");
        this.store = store;
        this.appName = applicationName;

        this.providerMap = new ConcurrentHashMap<>();
        this.parentNode = String.join("/", Constants.STORE_ROOT, appName);
    }

    public Collection<ProviderInfo> getServiceList() {
        return providerMap.values();
    }

    public ServerListUpdater getKvStoreBasedUpdater() {
        return new RegistryServerListUpdater(store, parentNode, providerMap);
    }

    public ServerListUpdater getKvStoreBasedUpdater(ExecutorService executorService) {
        return new RegistryServerListUpdater(store, parentNode, providerMap)
                .setExecutorService(executorService);
    }

    @Override
    public List<RegisteredServer> getInitialListOfServers() {
        return obtainServerListByKvStore();
    }

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
                .map(i -> new RegisteredServer(appName, i))
                .collect(Collectors.toList());
    }
}
