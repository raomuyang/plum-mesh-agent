package cn.suniper.mesh.discovery.commons;

import cn.suniper.mesh.discovery.KVStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
public class KvSource {

    /**
     * 根据runtime环境返回 KV store 的类型，若没有引入任何相关的依赖，则抛出{@link ClassNotFoundException}
     * 若存在多个，会按照字母顺序返回第一个
     *
     * @return {@link Provider}
     * @throws ClassNotFoundException 缺乏任何相关的依赖
     */
    public static Provider getSourceType() throws ClassNotFoundException {
        if (Provider.ETCD.isPresent()) {
            return Provider.ETCD;
        } else if (Provider.ZOOKEEPER.isPresent()) {
            return Provider.ZOOKEEPER;
        } else {
            throw new ClassNotFoundException("The kv store (zk/etcd) dependency is required.");
        }
    }

    public enum Provider {
        ETCD("etcd", "cn.suniper.mesh.discovery.provider.EtcdStore"),
        ZOOKEEPER("zookeeper", "cn.suniper.mesh.discovery.provider.ZKStore"),
        AUTO("auto", "");

        static Map<Provider, Class<? extends KVStore>> classMap = new ConcurrentHashMap<>();

        String storeName;
        String className;
        boolean absentChecked;

        Provider(String name, String className) {
            this.storeName = name;
            this.className = className;
        }

        public String storeName() {
            return storeName;
        }

        public String className() {

            return this.className;
        }

        @SuppressWarnings("unchecked")
        public boolean isPresent() {

            return Optional.ofNullable((Class) classMap.get(this)).orElseGet(() -> {
                if (absentChecked) return null;
                absentChecked = true;
                try {
                    if (this == AUTO) {
                        Provider present = getSourceType();
                        Class<? extends KVStore> type = present.getType();
                        classMap.put(this, type);
                        return type;
                    } else {
                        Class<? extends KVStore> clazz = (Class<? extends KVStore>) Class.forName(this.className());
                        Optional.ofNullable(clazz).ifPresent(c -> classMap.put(this, clazz));
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }) != null;
        }

        public Class<? extends KVStore> getType() {
            return isPresent() ? classMap.get(this) : null;
        }

    }
}
