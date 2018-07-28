package cn.suniper.mesh.discovery.annotation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * config properties
 *
 * @author Rao Mengnan
 *         on 2018/7/6.
 */
public enum CommonPropertyName {
    RIBBON_CLIENT_NAME("plum", "", Object.class),
    PREFIX("plum.app", "", Object.class),
    APP_NAME("name", "name", String.class),
    SERVER_GROUP("group", "serverGroup", String.class),
    REGISTRY_LIST("registries", "registryUrlList", List.class),
    LISTEN_IP("host", "host", String.class),
    LISTEN_PORT("port", "port", int.class),
    PROVIDER_WEIGHT("weight", "weight", int.class),
    VERSION("version", "version", int.class);


    private static Map<String, CommonPropertyName> map = new ConcurrentHashMap<>();
    private String name;
    private String fieldName;
    private Class<?> type;

    CommonPropertyName(String name, String fieldName, Class<?> type) {
        this.name = name;
        this.type = type;
        this.fieldName = fieldName;
    }

    public String propName() {
        if (this == PREFIX || this == RIBBON_CLIENT_NAME) return name;
        return String.join(".", PREFIX.propName(), name);
    }

    public String fieldName() {
        return this.fieldName;
    }

    public Class<?> type() {
        return type;
    }

    public static CommonPropertyName get(String value) {
        return map.computeIfAbsent(value, k -> {
            for (CommonPropertyName prop : CommonPropertyName.values()) {
                if (prop.propName().equals(k)) {
                    return prop;
                }
            }
            return null;
        });
    }
}
