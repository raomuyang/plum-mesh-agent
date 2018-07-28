package cn.suniper.mesh.discovery.util;

import cn.suniper.mesh.discovery.model.Node;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Optional;

/**
 * Mapper of {@link Node} and {@link ProviderInfo}
 *
 * @author Rao Mengnan
 *         on 2018/6/11.
 */
public class MapperUtil {

    private static Log log = LogFactory.getLog(MapperUtil.class);

    /**
     * Map node to providerInfo (update existing provider information)
     * ip/port[/weight] to {@link ProviderInfo}
     * @param node KvStore中的节点信息
     * @param provider 服务提供者信息
     */
    public static void node2Provider(Node node, ProviderInfo provider) {
        String key = node.getKey();
        String value = node.getValue();
        String[] store = Optional.ofNullable(value).orElse("").trim().split("/");

        if (store.length != 2 && store.length != 3) {
            throw new IllegalArgumentException(String.format("illegal record: key: %s, value: %s", key, value));
        }

        try {
            int port = Integer.valueOf(store[1]);
            int weight = store.length == 3 ? Integer.valueOf(store[2]) : 0;
            provider.setPort(port);
            provider.setWeight(weight);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("illegal record: illegal port or weight: key: %s, value: %s",
                    key, value));
        }

        String name = new File(key).getName();
        provider.setName(name);
        provider.setIp(store[0]);
        provider.setVersion(node.getVersion());
    }

    /**
     * Map node to providerInfo
     * @param node the information of node in the kv store
     * @return {@link ProviderInfo} or null
     */
    public static ProviderInfo node2Provider(Node node) {
        try {
            ProviderInfo provider = new ProviderInfo();
            node2Provider(node, provider);
            return provider;
        } catch (IllegalArgumentException e) {
            log.debug(e);
            return null;
        }
    }
}
