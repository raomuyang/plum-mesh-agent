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

}
