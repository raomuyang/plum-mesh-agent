package cn.suniper.mesh.transport.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rao Mengnan
 *         on 2018/7/2.
 */
public class DefaultPipelineInitializer implements Initializer {

    private List<Class<? extends ChannelHandler>> channelHandlers;

    public DefaultPipelineInitializer() {
    }

    @SuppressWarnings("unchecked")
    public DefaultPipelineInitializer(List<String> handlerList) throws ClassNotFoundException {
        channelHandlers = new ArrayList<>();
        for (String handlerClassName: handlerList) {
            Class<? extends ChannelHandler> handlerClass = (Class<? extends ChannelHandler>) Class.forName(handlerClassName);
            channelHandlers.add(handlerClass);
        }
    }

    public void setChannelHandlers(List<Class<? extends ChannelHandler>> channelHandlers) {
        this.channelHandlers = channelHandlers;
    }

    /**
     * 初始化channel
     * @param channel SocketChannel
     * @throws IllegalAccessException 无法找到相应的类
     * @throws InstantiationException 无法找到相应的类
     */
    @Override
    public void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        for (Class<? extends ChannelHandler> handlerClass: channelHandlers) {
            ChannelHandler handler = handlerClass.newInstance();
            pipeline.addLast(handler);
        }
    }
}
