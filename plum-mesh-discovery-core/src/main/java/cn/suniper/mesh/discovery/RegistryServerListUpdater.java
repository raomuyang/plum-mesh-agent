package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import cn.suniper.mesh.discovery.util.MapperUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.loadbalancer.ServerListUpdater;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Service list updater, which updates the service list in real time through kvStore monitoring of child nodes.
 * <p>
 * RegistryServerListUpdater is bound to a providerInfoMap, which must be thread-safe.
 * <p>
 * When each updater instance starts an update, it will listen to the child nodes under /conf/suniper/{@code serverGroup} through the watcher.
 * <p>
 * The call to {@link RegistryServerListUpdater#start(UpdateAction)} is idempotent.
 * <p>
 * By default, each updater will run in the daemon thread after startup. Of course, you can customize the runtime thread by {@link RegistryServerListUpdater#setExecutorService(ExecutorService)}.
 * <p>
 * By default, the error will not be processed. If necessary, it can be set by {@link RegistryServerListUpdater#setOnError(Consumer)}.
 * <p>
 * <p>
 * 服务列表更新器，通过kvStore对子节点的监控，实时更新服务列表。
 * <p>
 * RegistryServerListUpdater绑定一个providerInfoMap，这个map必须是线程安全的。
 * <p>
 * 每个updater实例启动更新时，会通过watcher监听/conf/suniper/{@code serverGroup}下的子节点。
 * <p>
 * {@link RegistryServerListUpdater#start(UpdateAction)}的调用是幂等的。
 * <p>
 * 默认情况下，每个updater启动後会运行在守护线程中，当然也可以通过{@link RegistryServerListUpdater#setExecutorService(ExecutorService)}自定义运行时的线程。
 * <p>
 * 默认情况下出错不会做任何处理，如有需要可以通过{@link RegistryServerListUpdater#setOnError(Consumer)} 设置。
 *
 * @author Rao Mengnan
 *         on 2018/6/11.
 */
public class RegistryServerListUpdater implements ServerListUpdater {

    private static final int DEFAULT_CORE_POOL_SIZE = 2;
    private static final int DEFAULT_QUEUE_SIZE = 1000;
    private static final ThreadPoolExecutor defaultPoolExecutor;
    private static final Map<String, Stoppable> watching;

    private static Log log = LogFactory.getLog(RegistryServerListUpdater.class);

    static {
        watching = new ConcurrentHashMap<>();
        defaultPoolExecutor = new ThreadPoolExecutor(
                DEFAULT_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE * 5,
                0L, TimeUnit.NANOSECONDS,
                new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE),
                new ThreadFactoryBuilder()
                        .setNameFormat("SuniperRegistryServiceListUpdater-%d")
                        .setDaemon(true).build());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutdown the executor of Suniper registry-updater");
            defaultPoolExecutor.shutdown();
        }));
    }

    private KVStore store;
    private String parentNode;
    private Map<String, ProviderInfo> providerInfoMap;
    private Consumer<Throwable> onError = Stoppable.DEFAULT_ON_ERROR;

    private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());
    private ExecutorService executorService;

    public RegistryServerListUpdater(KVStore store, String parentNode, Map<String, ProviderInfo> providerInfoMap) {
        this.store = store;
        this.parentNode = parentNode;
        this.providerInfoMap = providerInfoMap;
        this.executorService = defaultPoolExecutor;
    }

    public RegistryServerListUpdater setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public RegistryServerListUpdater setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    @Override
    public void start(UpdateAction updateAction) {
        if (Optional.ofNullable(watching.get(parentNode)).map(Stoppable::get).orElse(false)) {
            log.info(String.format("Node %s is watching", parentNode));
            return;
        }

        Holder holder = new Holder(updateAction, providerInfoMap);
        Stoppable stoppable = new Stoppable();
        defaultPoolExecutor.execute(() -> {
            try {
                store.watchChildren(parentNode, stoppable, holder);
            } catch (Exception e) {
                log.warn(String.format("Watch exception of %s", parentNode), e);
                onError.accept(e);
            }
        });
        watching.put(parentNode, stoppable);
    }

    @Override
    public void stop() {
        Stoppable stoppable = watching.get(parentNode);
        if (stoppable != null) {
            stoppable.stop();
        } else {
            log.warn(String.format("Stoppable not found: %s", parentNode));
        }
    }

    @Override
    public String getLastUpdate() {
        return new Date(lastUpdated.get()).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return System.currentTimeMillis() - lastUpdated.get();
    }

    @Override
    public int getNumberMissedCycles() {
        return 0;
    }

    @Override
    public int getCoreThreads() {
        if (Optional.ofNullable(watching.get(parentNode)).map(Stoppable::get).orElse(false)) {
            return 0;
        } else if (!(executorService instanceof ThreadPoolExecutor)) {
            return 0;
        } else {
            return ((ThreadPoolExecutor) executorService).getCorePoolSize();
        }
    }

    static class Stoppable implements Supplier<Boolean> {
        private static final Consumer<Throwable> DEFAULT_ON_ERROR = (c) -> {
        };

        private boolean exit;


        @Override
        public Boolean get() {
            return exit;
        }

        void stop() {
            this.exit = true;
        }

    }

    private class Holder implements BiConsumer<Event, Node> {
        private Map<String, ProviderInfo> providerInfoMap;
        private UpdateAction updateAction;

        Holder(UpdateAction updateAction, Map<String, ProviderInfo> providerInfoMap) {
            this.providerInfoMap = providerInfoMap;
            this.updateAction = updateAction;
        }

        @Override
        public void accept(Event event, Node node) {
            String key = node.getKey();
            switch (event) {
                case DELETE:
                    ProviderInfo removed = providerInfoMap.remove(key);
                    if (removed != null)
                        log.info(String.format("Service offline: %s - %s:%s", key, removed.getIp(), removed.getPort()));
                    else
                        log.info("Service offline: %s - no such provide cache");
                    update();
                    break;
                case UPDATE:
                    ProviderInfo oldInfo = providerInfoMap.computeIfAbsent(key, k -> new ProviderInfo());
                    MapperUtil.node2Provider(node, oldInfo);
                    update();
                    break;
                default:
                    log.info(String.format("unrecognized event: %s, key: %s", event, node.getKey()));

            }
        }

        private void update() {
            RegistryServerListUpdater.this.lastUpdated.set(System.currentTimeMillis());
            updateAction.doUpdate();
        }

    }

}
