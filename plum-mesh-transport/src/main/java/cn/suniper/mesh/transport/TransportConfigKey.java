package cn.suniper.mesh.transport;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
public enum TransportConfigKey {
    GROUP_EVENT_TYPE("plum.tcp.groupEventType", "groupEventType", String.class),
    SOCKET_CHANNEL_TYPE("plum.tcp.socketChannelType", "socketChannelType", String.class),
    WORKERS("plum.tcp.workers", "workers", int.class),
    MAX_POOL_CONN("plum.tcp.maxPoolConn", "maxPoolConn", int.class),
    CHANNEL_PIPELINES("plum.tcp.channelPipelines", "channelPipelines", List.class);

    private static Map<String, TransportConfigKey> map = new ConcurrentHashMap<>();

    private String fieldName;
    private Class type;
    private String propName;

    TransportConfigKey(String propName, String fieldName, Class type) {
        this.propName = propName;
        this.fieldName = fieldName;
        this.type = type;
    }

    public String propName() {
        return propName;
    }

    public String fieldName() {
        return fieldName;
    }

    public Class getType() {
        return type;
    }

    public static TransportConfigKey get(String fieldName) {
        return map.computeIfAbsent(fieldName, k -> {
            for (TransportConfigKey configKey: TransportConfigKey.values()) {
                if (configKey.fieldName.equals(k)) return configKey;
            }
            return null;
        });
    }

    public Object convert(Object val) {
        if (type == String.class) {
            return String.valueOf(val);
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(String.valueOf(val));
        } else if (type == List.class) {
            if (List.class.isAssignableFrom(val.getClass())) {
                return val;
            } else if (val.getClass() == String[].class) {
                String[] args = (String[]) val;
                return Arrays.asList(args);
            }
            String listStr = String.valueOf(val);
            String[] values = listStr.split(",");
            return Arrays.stream(values).map(String::trim).collect(Collectors.toList());
        }
        return val;
    }

}
