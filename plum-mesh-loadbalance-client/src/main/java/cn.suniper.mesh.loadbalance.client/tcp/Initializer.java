package cn.suniper.mesh.loadbalance.client.tcp;

import io.netty.channel.Channel;

/**
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public interface Initializer {
    void initChannel(Channel channel) throws Exception;
}
