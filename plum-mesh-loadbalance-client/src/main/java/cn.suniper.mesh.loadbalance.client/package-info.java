/**
 * http包中使用 okhttp 实现了基于ribbon的负载均衡客户端
 * tcp包中使用 netty 实现了基于ribbon的负载均衡客户端，客户端为异步通信客户端，
 * {@link cn.suniper.mesh.loadbalance.client.tcp.AsyncTcpResponse} 返回的是channelFuture
 *
 * @author Rao Mengnan
 * on 2018/7/3.
 */
package cn.suniper.mesh.loadbalance.client;