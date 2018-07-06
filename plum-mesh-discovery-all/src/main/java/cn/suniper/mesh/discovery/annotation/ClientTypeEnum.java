package cn.suniper.mesh.discovery.annotation;

/**
 * {@link ClientTypeEnum#ASYNC_TCP}:  {@link cn.suniper.mesh.transport.tcp.AsyncLoadBalancingTcpClient}
 * {@link ClientTypeEnum#OKHTTP}:  {@link cn.suniper.mesh.transport.http.LoadBalancingHttpClient}
 * {@link ClientTypeEnum#DEFAULT}:  {@link com.netflix.niws.client.http.RestClient}
 *
 * @author Rao Mengnan
 *         on 2018/7/7.
 */
public enum ClientTypeEnum {
    ASYNC_TCP,
    OKHTTP,
    DEFAULT
}
