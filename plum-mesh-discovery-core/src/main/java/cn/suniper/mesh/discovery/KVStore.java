package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Event;
import cn.suniper.mesh.discovery.model.Node;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public interface KVStore extends AutoCloseable {
    /**
     * 获取节点及数据，当不存在时，返回null
     *
     * @param key 节点名称
     * @return Node info
     * @throws Exception exception during operation
     */
    Node get(String key) throws Exception;

    /**
     * 列出所有子节点及其数据，当父节点不存在或没有子节点时，返回空列表
     *
     * @param prefix 父节点
     * @return List of Node
     * @throws Exception exception during operation
     */
    List<Node> list(String prefix) throws Exception;

    /**
     * 列出所有子节点的名称，当父节点不存在或没有子节点时，返回空列表
     *
     * @param prefix 父节点
     * @return List of node name
     * @throws Exception exception during operation
     */
    List<String> listKeys(String prefix) throws Exception;

    /**
     * 将数据更新到节点中，持久化存储。节点已存在则更新，但不修改节点的类型（临时/持久）
     *
     * @param key   节点名称 （配置所在目录）
     * @param value 节点数据 （注册服务信息）
     * @return reversion
     */
    long put(String key, String value) throws Exception;

    /**
     * 将数据更新到节点中，持久化存储。节点已存在则更新，但不修改节点的类型（临时/持久）
     *
     * @param key   节点名称 （配置所在目录）
     * @param value 节点数据 （注册服务信息）
     * @param ephemeral 是否为临时节点
     * @return reversion
     * @throws Exception exception during operation
     */
    long put(String key, String value, boolean ephemeral) throws Exception;

    /**
     * 删除节点，
     * @param key 节点名称
     * @return 成功删除的节点数
     * @throws Exception IllegalArgumentException: 节点不为空
     * @throws Exception exception during operation
     */
    long delete(String key) throws Exception;

    /**
     * 节点是否存在
     * @param key 节点名称
     * @return true: 存在
     */
    boolean exists(String key) throws Exception;

    /**
     * 监视所有子节点的变化（不包含父节点），持续监听
     * @param key 父节点名称
     * @param consumer 子节点变化时的回调
     * @throws Exception exception during operation
     */
    void watchChildren(String key, BiConsumer<Event, Node> consumer) throws Exception;

    /**
     * 监视所有子节点的变化（不包含父节点），根据退出信号判断是否需要退出监听
     * @param key 父节点名称
     * @param exitSignSupplier 提供退出信号，否则一直监听
     * @param consumer 子节点变化时的回调
     * @throws Exception exception during operation
     */
    void watchChildren(String key, Supplier<Boolean> exitSignSupplier, BiConsumer<Event, Node> consumer) throws Exception;

    /**
     * 用于创建父节点（prefix）, 且为持久节点
     * @param parentNode 父节点名称
     * @throws Exception exception during operation
     */
    void createParentNode(String parentNode) throws Exception;
}
