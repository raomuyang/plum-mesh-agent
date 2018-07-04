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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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
        if (application == null) throw new IllegalArgumentException("application must be not null");
        init(store, application.getName());
    }

    public RegisteredServerDynamicList(KVStore store, String applicationName) {
        init(store, applicationName);
    }

    private void init(KVStore store, String applicationName) {
        this.store = store;
        this.appName = applicationName;

        this.providerMap = new ConcurrentHashMap<>();
        this.parentNode = String.join("/", Constants.STORE_ROOT, appName);
    }

    public Collection<ProviderInfo> getServiceList() {
        return providerMap.values();
    }

    public ServerListUpdater getUpdater() {
        return new RegistryServerListUpdater(store, parentNode, providerMap);
    }

    public ServerListUpdater getUpdater(ExecutorService executorService) {
        return new RegistryServerListUpdater(store, parentNode, providerMap)
                .setExecutorService(executorService);
    }

    @Override
    public List<RegisteredServer> getInitialListOfServers() {
        try {
            List<Node> nodeInfoList = store.list(parentNode);
            Stream<ProviderInfo> stream = nodeInfoList.stream().map(node ->
                    providerMap.computeIfAbsent(node.getKey(), k -> MapperUtil.node2Provider(node)));
            return map2ServerList(stream);

        } catch (Exception e) {
            log.warn(String.format("get initial list of servers failed: %s", parentNode), e);
            return Lists.newArrayList();
        }
    }

    @Override
    public List<RegisteredServer> getUpdatedListOfServers() {
        return map2ServerList(providerMap.values().stream());
    }

    private List<RegisteredServer> map2ServerList(Stream<ProviderInfo> stream) {
        return stream
                .filter(Objects::nonNull)
                .map(i -> new RegisteredServer(appName, i))
                .collect(Collectors.toList());
    }
}
