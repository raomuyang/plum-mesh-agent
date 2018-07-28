package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The interface to operating kv store
 *
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public interface KVStore extends AutoCloseable {
    /**
     * Get node and its data, return null when it does not exist
     *
     * @param key node name
     * @return Node info
     * @throws Exception exception during operation
     */
    Node get(String key) throws Exception;

    /**
     * List all child nodes and their data, return an empty list when
     * the parent node does not exist or has no children
     *
     * @param prefix 父节点
     * @return List of Node
     * @throws Exception exception during operation
     */
    List<Node> list(String prefix) throws Exception;

    /**
     * List the names of all child nodes,
     * return an empty list when the parent node does not exist or has no children
     *
     * @param prefix 父节点
     * @return List of node name
     * @throws Exception exception during operation
     */
    List<String> listKeys(String prefix) throws Exception;

    /**
     * Update the data to the node and persist the storage.
     * <p>
     * Update if the node already exists,
     * but does not modify the type of the node (temporary/persistent)
     *
     * @param key   Node name (configuration directory)
     * @param value Node data (registered service information)
     * @return reversion
     * @throws Exception exception during operation
     */
    long put(String key, String value) throws Exception;

    /**
     * Update data to nodes and persist storage.
     * <p>
     * Update if the node already exists,
     * but does not modify the type of the node (temporary/persistent)
     *
     * @param key       node name (configuration directory)
     * @param value     node data (registered service information)
     * @param ephemeral is a temporary node
     * @return reversion
     * @throws Exception exception during operation
     */
    long put(String key, String value, boolean ephemeral) throws Exception;

    /**
     * Delete the node
     *
     * @param key node name
     * @return Number of nodes successfully deleted
     * @throws Exception IllegalArgumentException: Node is not empty
     * @throws Exception exception during operation
     *                        
     */
    long delete(String key) throws Exception;

    /**
     * Whether the node exists
     * <p>
     * 节点是否存在
     *
     * @param key node name 节点名称
     * @return true: exists 存在
     * @throws Exception exception during operation
     */
    boolean exists(String key) throws Exception;

    /**
     * Monitor changes to all child nodes (without parent nodes), continuous monitoring
     * <p>
     * 监视所有子节点的变化（不包含父节点），持续监听
     *
     * @param key      Name of parent node 父节点名称
     * @param consumer Callback when child nodes change 子节点变化时的回调
     * @throws Exception exception during operation
     */
    void watchChildren(String key, BiConsumer<Event, Node> consumer) throws Exception;

    /**
     * Monitor the changes of all child nodes (excluding the parent node),
     * and judge whether it needs to exit the monitoring according to the exit signal.
     * <p>
     * 监视所有子节点的变化（不包含父节点），根据退出信号判断是否需要退出监听
     *
     * @param key              Name of parent node 父节点名称
     * @param exitSignSupplier Provide an exit signal, otherwise it will always listen 提供退出信号，否则一直监听
     * @param consumer         子节点变化时的回调
     * @throws Exception exception during operation
     */
    void watchChildren(String key, Supplier<Boolean> exitSignSupplier, BiConsumer<Event, Node> consumer) throws Exception;

    /**
     * Used to create a parent (prefix) and a persistent node
     * <p>
     * 用于创建父节点（prefix）, 且为持久节点
     *
     * @param parentNode Name of parent node 父节点名称
     * @throws Exception exception during operation
     */
    void createParentNode(String parentNode) throws Exception;
}
