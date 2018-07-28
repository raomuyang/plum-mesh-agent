package cn.suniper.mesh.discovery.model;

/**
 * This class corresponds to the key-value pair and version information in the node.
 * key: /config/suniper/YOUR_GROUP/SERVICE_ID
 * value: ip/port/weight
 * createReversion: czxid in zk or createReversion in etcd, etc.
 * modReversion: mzxid in zk or modReversion in etcd, etc.
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class Node {
    private String key;
    private String value;
    private long createReversion;
    private long modReversion;
    private long version;

    public Node() {
    }

    public Node(String key, String value, long modVersion) {
        this.key = key;
        this.value = value;
        this.modReversion = modVersion;
    }

    public Node(String key, String value, long createReversion, long modReversion, long version) {
        this.key = key;
        this.value = value;
        this.createReversion = createReversion;
        this.modReversion = modReversion;
        this.version = version;
    }

    public long getCreateReversion() {
        return createReversion;
    }

    public void setCreateReversion(long createReversion) {
        this.createReversion = createReversion;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getModReversion() {
        return modReversion;
    }

    public void setModReversion(long modReversion) {
        this.modReversion = modReversion;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node nodeInfo = (Node) o;

        if (createReversion != nodeInfo.createReversion) return false;
        if (modReversion != nodeInfo.modReversion) return false;
        if (version != nodeInfo.version) return false;
        if (!key.equals(nodeInfo.key)) return false;
        return value.equals(nodeInfo.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (int) (createReversion ^ (createReversion >>> 32));
        result = 31 * result + (int) (modReversion ^ (modReversion >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", createReversion=" + createReversion +
                ", modReversion=" + modReversion +
                ", version=" + version +
                '}';
    }
}
