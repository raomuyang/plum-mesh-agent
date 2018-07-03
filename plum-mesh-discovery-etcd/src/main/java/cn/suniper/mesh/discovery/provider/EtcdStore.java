package cn.suniper.mesh.discovery.provider;

import cn.suniper.mesh.discovery.KVStore;
import cn.suniper.mesh.discovery.exception.NodeNotEmptyException;
import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.lease.LeaseGrantResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * etcd v3 protocol
 *
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class EtcdStore implements KVStore {

    private static Log log = LogFactory.getLog(EtcdStore.class);

    private static final int EPHEMERAL_LEASE = 60;

    private Client client;
    private long leaseId;

    public EtcdStore(Client client) throws ExecutionException, InterruptedException {
        this.client = client;
        initLease();
    }

    private void initLease() throws ExecutionException, InterruptedException {
        Lease lease = client.getLeaseClient();
        LeaseGrantResponse response = lease.grant(EPHEMERAL_LEASE).get();
        leaseId = response.getID();
        lease.keepAlive(leaseId);
    }

    @Override
    public Node get(String key) throws ExecutionException, InterruptedException {
        log.debug(String.format("get node info of %s", key));
        KV kv = client.getKVClient();
        ByteSequence storeKey =
                Optional.ofNullable(key)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        GetResponse response = kv.get(storeKey).get();
        log.debug(response.getHeader());
        return response.getKvs().stream()
                .map(EtcdStore::kv2NodeInfo)
                .findFirst().orElse(null);

    }

    @Override
    public List<Node> list(String prefix) throws ExecutionException, InterruptedException {
        log.debug(String.format("list node info of %s", prefix));
        KV kv = client.getKVClient();
        ByteSequence storePrefix =
                Optional.ofNullable(prefix)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        GetOption option = GetOption.newBuilder().withPrefix(storePrefix).build();
        GetResponse response = kv.get(storePrefix, option).get();
        return response.getKvs().stream()
                .filter(o -> !o.getKey().equals(storePrefix))
                .map(EtcdStore::kv2NodeInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listKeys(String prefix) throws ExecutionException, InterruptedException {
        KV kv = client.getKVClient();
        ByteSequence storePrefix =
                Optional.ofNullable(prefix)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        GetOption option = GetOption.newBuilder()
                .withKeysOnly(true)
                .withPrefix(storePrefix).build();
        GetResponse response = kv.get(storePrefix, option).get();
        return response.getKvs().stream()
                .map(o -> o.getKey().toStringUtf8())
                .filter(k -> !k.equals(prefix))
                .collect(Collectors.toList());
    }

    @Override
    public long put(String key, String value) throws ExecutionException, InterruptedException {
        return put(key, value, false);
    }

    @Override
    public long put(String key, String value, boolean ephemeral) throws ExecutionException, InterruptedException {
        log.debug(String.format("put %s to %s", value, key));
        KV kv = client.getKVClient();
        ByteSequence storeKey =
                Optional.ofNullable(key)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        ByteSequence storeValue =
                Optional.ofNullable(value)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        PutOption.Builder builder = PutOption.newBuilder();
        if (ephemeral) builder.withLeaseId(leaseId);

        PutResponse response = kv.put(storeKey,
                storeValue, builder.build()).get();
        log.info(String.format("put key-value: key: %s, reversion: %s, has-prev: %s, ephemeral: %s",
                key, response.getHeader().getRevision(), response.hasPrevKv(), ephemeral));
        return response.getHeader().getRevision();
    }

    @Override
    public long delete(String key) throws ExecutionException, InterruptedException {
        log.info(String.format("delete node: %s", key));
        if (listKeys(key).size() > 0) {
            throw new NodeNotEmptyException("Node not empty: " + key);
        }

        KV kv = client.getKVClient();
        ByteSequence storeKey =
                Optional.ofNullable(key)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        DeleteResponse response = kv.delete(storeKey).get();
        return response.getDeleted();
    }

    @Override
    public boolean exists(String key) throws ExecutionException, InterruptedException {
        log.debug(String.format("check exists: %s", key));
        KV kv = client.getKVClient();
        ByteSequence storeKey =
                Optional.ofNullable(key)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        GetOption option = GetOption.newBuilder().withCountOnly(true).build();
        GetResponse response = kv.get(storeKey, option).get();
        log.debug(String.format("exists: %s ? : %s", key, response.getCount()));
        return response.getCount() == 1;
    }

    @Override
    public void watchChildren(String key, BiConsumer<Event, Node> consumer) throws InterruptedException {
        watchChildren(key, () -> false, consumer);
    }

    @Override
    public void watchChildren(String key, Supplier<Boolean> exitSignSupplier, BiConsumer<Event, Node> consumer) throws InterruptedException {
        ByteSequence storeKey =
                Optional.ofNullable(key)
                        .map(ByteSequence::fromString)
                        .orElse(null);

        try (Watch watch = client.getWatchClient();
             Watch.Watcher watcher = watch.watch(storeKey,
                     WatchOption.newBuilder().withPrefix(storeKey).build())) {
            while (!exitSignSupplier.get()) {
                WatchResponse response = watcher.listen();
                response.getEvents().forEach(watchEvent -> {
                    // 跳过根节点的变化
                    if (watchEvent.getKeyValue().getKey().equals(storeKey)) return;
                    Event event;
                    switch (watchEvent.getEventType()) {
                        case PUT:
                            event = Event.UPDATE;
                            break;
                        case DELETE:
                            event = Event.DELETE;
                            break;
                        default:
                            event = Event.UNRECOGNIZED;
                    }
                    KeyValue keyValue = watchEvent.getKeyValue();
                    Node info = kv2NodeInfo(keyValue);
                    consumer.accept(event, info);
                });
            }
        }
    }

    @Override
    public void createParentNode(String parentNode) throws ExecutionException, InterruptedException {
        log.debug(String.format("check exists: %s", parentNode));
        KV kv = client.getKVClient();
        ByteSequence storeKey =
                Optional.ofNullable(parentNode)
                        .map(ByteSequence::fromString)
                        .orElse(null);
        if (exists(parentNode)) return;
        PutResponse response = kv.put(storeKey, ByteSequence.fromString("")).get();
        log.info(String.format("create parent nodes: %s, cluster: %s, member: %s",
                parentNode,
                response.getHeader().getClusterId(),
                response.getHeader().getMemberId()));
    }

    static Node kv2NodeInfo(KeyValue kv) {
        String key = kv.getKey().toStringUtf8();
        String value = Optional.ofNullable(kv.getValue()).map(ByteSequence::toStringUtf8).orElse("");
        return new Node(key, value, kv.getCreateRevision(), kv.getModRevision(), kv.getVersion());
    }

}
