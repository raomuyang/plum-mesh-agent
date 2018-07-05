package cn.suniper.mesh.discovery.provider;

import cn.suniper.mesh.discovery.KVStore;
import cn.suniper.mesh.discovery.exception.NodeNotEmptyException;
import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Rao Mengnan
 *         on 2018/6/15.
 */
public class ZKStore implements KVStore {
    private static Log log = LogFactory.getLog(ZKStore.class);
    private static final String DEFAULT_NODE_VALUE = "";

    private ZooKeeper zooKeeper;

    public ZKStore(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public Node get(String key) throws Exception {
        return get(key, null);
    }

    @Override
    public List<Node> list(String prefix) throws Exception {
        List<String> keys = listKeys(prefix);
        List<Node> nodes = new ArrayList<>();
        for (String key : keys) {
            nodes.add(get(key));
        }
        return nodes;
    }

    @Override
    public List<String> listKeys(String prefix) throws Exception {
        return listKeys(prefix, null);
    }

    @Override
    public long put(String key, String value) throws Exception {
        return put(key, value, false);
    }

    @Override
    public long put(String key, String value, boolean ephemeral) throws Exception {
        Stat stat = zooKeeper.exists(key, null);
        if (stat != null) {
            zooKeeper.setData(key, value.getBytes(), stat.getVersion());
        } else {
            CreateMode mode = ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;
            zooKeeper.create(key, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
        }

        stat = zooKeeper.exists(key, null);
        log.info(String.format("current stat: %s", stat));
        return stat.getMzxid();
    }

    @Override
    public long delete(String key) throws Exception {
        Stat stat = zooKeeper.exists(key, null);
        if (stat == null) {
            return 0;
        } else if (stat.getNumChildren() > 0) {
            throw new NodeNotEmptyException("Node not empty: " + key);
        }
        zooKeeper.delete(key, -1);
        return 1;
    }

    @Override
    public boolean exists(String key) throws Exception {
        return zooKeeper.exists(key, null) != null;
    }

    @Override
    public void watchChildren(String key, BiConsumer<Event, Node> consumer) throws Exception {
        this.watchChildren(key, null, consumer);
    }

    @Override
    public void watchChildren(String key, Supplier<Boolean> exitSignSupplier, BiConsumer<Event, Node> consumer) throws Exception {
        List<String> res = listKeys(key, new ChildrenWatcher(key, consumer, exitSignSupplier));
        log.debug(res);
    }

    @Override
    public void createParentNode(String parentNode) throws Exception {
        PathUtils.validatePath(parentNode);
        if (parentNode.equals("/")) return;
        File node = new File(parentNode);
        String parent = node.getParent();
        if (!exists(parent)) {
            createParentNode(parent);
        }
        put(parentNode, DEFAULT_NODE_VALUE);
    }

    private List<String> listKeys(String prefix, Watcher watcher) throws Exception {
        log.debug("list keys: " + prefix);
        Stat stat = zooKeeper.exists(prefix, watcher);
        if (stat != null) {
            List<String> keys = new ArrayList<>();
            zooKeeper
                    .getChildren(prefix, watcher).forEach(
                            s -> keys.add(String.join("/", prefix, s))
                    );
            return keys;
        }
        return Lists.newArrayList();
    }

    private Node get(String key, Watcher watcher) throws Exception {
        try {
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(key, watcher, stat);
            if (data == null) data = new byte[0];
            return new Node(key, new String(data), stat.getCzxid(), stat.getMzxid(), stat.getVersion());
        } catch (KeeperException.NoNodeException e) {
            log.info(String.format("no such node: %s", key));
            log.debug(e);
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        Optional.ofNullable(zooKeeper).ifPresent(z -> {
            try {
                z.close();
            } catch (InterruptedException e) {
                log.debug("close zookeeper client interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 监视节点及子节点的变化：
     * 当前节点发生改变时，不做任何处理；
     * 子节点发生变化时，创建相应的sub-node watcher监听子节点的变化，并调用回调通知变化的信息
     */
    class ChildrenWatcher implements Watcher {

        private final Supplier<Boolean> DEFAULT_SUPPLIER = () -> false;

        private ConcurrentHashMap<String, SubWatcher> childrenWatcher;
        private BiConsumer<cn.suniper.mesh.discovery.model.Event, Node> consumer;
        private String path;
        private Supplier<Boolean> exitSignSupplier;

        ChildrenWatcher(String path, BiConsumer<cn.suniper.mesh.discovery.model.Event, Node> consumer,
                               Supplier<Boolean> exitSignSupplier) {
            this.consumer = consumer;
            this.childrenWatcher = new ConcurrentHashMap<>();
            this.path = path;
            this.exitSignSupplier = exitSignSupplier == null ? DEFAULT_SUPPLIER : exitSignSupplier;
            this.listAndWatch(false);
        }

        @Override
        public void process(WatchedEvent event) {
            log.debug("parent-node: watch event: " + event);

            if (exitSignSupplier.get()) {
                log.info("parent-node: stop watch event: " + event);
                return;
            }

            switch (event.getType()) {
                case NodeChildrenChanged:
                    this.listAndWatch(true);
                    return;
                default:
                    log.debug("ignore event");
            }

            try {
                List<String> res = listKeys(path,this);
                log.debug(String.format("Sub nodes of %s: %s", path, res));
            } catch (Exception e) {
                log.warn("failed to keep watch node: " + path, e);
            }

        }

        private void listAndWatch(boolean accept) {
            try {
                List<String> subList = listKeys(path,this);

                log.debug(String.format("size of %s: %s", path, subList.size()));
                for (String sub: subList) {
                    SubWatcher watcher = childrenWatcher.computeIfAbsent(sub, k -> {
                        log.debug("create new watcher for " + sub);
                        SubWatcher newWatcher = new SubWatcher(consumer, exitSignSupplier);
                        newWatcher.stopWatch = true;
                        return newWatcher;
                    });

                    if (!watcher.stopWatch) continue;

                    log.debug("activate watcher for " + sub);
                    watcher.activate();

                    if (accept) {
                        Node node = get(sub, watcher);
                        consumer.accept(cn.suniper.mesh.discovery.model.Event.UPDATE, node);
                    } else {
                        zooKeeper.exists(sub, watcher, null, null);
                    }
                }

            } catch (Exception e) {
                log.warn("failed to list and watch node: " + path, e);
            }
        }
    }

    class SubWatcher implements Watcher {

        private BiConsumer<cn.suniper.mesh.discovery.model.Event, Node> consumer;
        private Supplier<Boolean> exitSignSupplier;
        private volatile boolean stopWatch;

        SubWatcher(BiConsumer<cn.suniper.mesh.discovery.model.Event, Node> consumer, Supplier<Boolean> exitSignSupplier) {
            this.consumer = consumer;
            this.exitSignSupplier = exitSignSupplier;
        }

        @Override
        public void process(WatchedEvent event) {
            if (exitSignSupplier.get()) {
                log.info("sub-node: stop watch event: " + event);
                return;
            }

            log.debug("sub-node: watch event: " + event);
            cn.suniper.mesh.discovery.model.Event wrapEvent;
            Node node;
            switch (event.getType()) {

                case NodeCreated:
                case NodeDataChanged:
                    wrapEvent = cn.suniper.mesh.discovery.model.Event.UPDATE;
                    try {
                        node = get(event.getPath(), this);
                        log.debug(String.format("get node(%s) data: ", event.getPath()) + node);

                    } catch (Throwable throwable) {
                        log.warn("error occurred in watcher", throwable);
                        return;
                    }
                    break;
                case NodeDeleted:
                    wrapEvent = cn.suniper.mesh.discovery.model.Event.DELETE;
                    node = new Node();
                    node.setKey(event.getPath());
                    break;
                default:
                    wrapEvent = cn.suniper.mesh.discovery.model.Event.UNRECOGNIZED;
                    node = new Node();
                    node.setKey(event.getPath());
            }

            stopWatch = true;
            consumer.accept(wrapEvent, node);
        }

        private SubWatcher activate() {
            this.stopWatch = false;
            return this;
        }
    }


}
