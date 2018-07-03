package cn.suniper.mesh.discovery.model;

/**
 * @author Rao Mengnan
 *         on 2018/6/11.
 */
public class ProviderInfo {
    // provider
    private String name;
    private String ip;
    private int weight;
    private int port;
    private long version;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

        ProviderInfo that = (ProviderInfo) o;

        if (weight != that.weight) return false;
        if (port != that.port) return false;
        if (version != that.version) return false;
        if (!name.equals(that.name)) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + weight;
        result = 31 * result + port;
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }
}