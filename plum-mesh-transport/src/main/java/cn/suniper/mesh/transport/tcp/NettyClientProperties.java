package cn.suniper.mesh.transport.tcp;

import java.util.List;

/**
 * @author Rao Mengnan
 *         on 2018/7/2.
 */
public class NettyClientProperties {
    private String groupEventType;
    private String socketChannelType;
    private int workers;
    private int maxPoolConn;
    private List<String> channelPipelines;

    public String getGroupEventType() {
        return groupEventType;
    }

    public void setGroupEventType(String groupEventType) {
        this.groupEventType = groupEventType;
    }

    public String getSocketChannelType() {
        return socketChannelType;
    }

    public void setSocketChannelType(String socketChannelType) {
        this.socketChannelType = socketChannelType;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getMaxPoolConn() {
        return maxPoolConn;
    }

    public void setMaxPoolConn(int maxPoolConn) {
        this.maxPoolConn = maxPoolConn;
    }

    public List<String> getChannelPipelines() {
        return channelPipelines;
    }

    public void setChannelPipelines(List<String> channelPipelines) {
        this.channelPipelines = channelPipelines;
    }

    @Override
    public String toString() {
        return "NettyClientProperties{" +
                "groupEventType='" + groupEventType + '\'' +
                ", socketChannelType='" + socketChannelType + '\'' +
                ", workers=" + workers +
                ", maxPoolConn=" + maxPoolConn +
                ", channelPipelines=" + channelPipelines +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NettyClientProperties that = (NettyClientProperties) o;

        if (workers != that.workers) return false;
        if (maxPoolConn != that.maxPoolConn) return false;
        if (groupEventType != null ? !groupEventType.equals(that.groupEventType) : that.groupEventType != null)
            return false;
        if (socketChannelType != null ? !socketChannelType.equals(that.socketChannelType) : that.socketChannelType != null)
            return false;
        return channelPipelines != null ? channelPipelines.equals(that.channelPipelines) : that.channelPipelines == null;
    }

    @Override
    public int hashCode() {
        int result = groupEventType != null ? groupEventType.hashCode() : 0;
        result = 31 * result + (socketChannelType != null ? socketChannelType.hashCode() : 0);
        result = 31 * result + workers;
        result = 31 * result + maxPoolConn;
        result = 31 * result + (channelPipelines != null ? channelPipelines.hashCode() : 0);
        return result;
    }
}
