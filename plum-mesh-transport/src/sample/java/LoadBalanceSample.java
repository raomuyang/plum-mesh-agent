import cn.suniper.mesh.transport.http.LoadBalancingHttpClient;
import cn.suniper.mesh.transport.tcp.*;
import com.netflix.client.ClientException;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * 这个例子展示了如何使用plum提供的两种负载均衡客户端.
 * 在本例子中创建请求时 Host 均标记为 LB-APP, 这是vipAddress，
 * 详情可见 {@link cn.suniper.mesh.transport.Constants}
 *
 * @author Rao Mengnan
 *         on 2018/7/4.
 */
public class LoadBalanceSample {

    /**
     * 对Http请求进行负载均衡:
     * 1. 使用固定列表初始化负载均衡器
     * 2. 使用负载均衡器初始化Http客户端
     * 3. 创建Http请求 {@link HttpRequest}
     *
     */
    private static void lbHttpRequest() throws ClientException {
        System.out.println("Load balance http request ----------------");
        ILoadBalancer loadBalancer = LoadBalancerBuilder
                .newBuilder()
                .withRule(new WeightedResponseTimeRule())
                .buildFixedServerListLoadBalancer(Arrays.asList(
                        new Server("atomicer.cn", 80),
                        new Server("baidu.com", 443),
                        new Server("zhihu.com", 443)
                ));

        LoadBalancingHttpClient httpClient = new LoadBalancingHttpClient(loadBalancer);

        HttpRequest request = new HttpRequest.Builder()
                .verb(HttpRequest.Verb.GET)
                .uri("https://LB-APP")
                .build();

        for (int i = 0; i < 10; i++) {
            HttpResponse response = httpClient.executeWithLoadBalancer(request);
            System.out.println(String.format("\n--------- %s ----------", response.getRequestedURI()));
            System.out.println(response.getStatus());
            System.out.println(response.getPayload());
        }
    }

    /**
     * 对Tcp请求进行负载
     * 注意，这里使用的是异步请求，按照netty的风格，所有的返回值在pipeline中处理或回调
     *
     * 1. 使用固定列表初始化负载均衡器
     * 2. 创建 {@link Initializer}实例用于初始化负载均衡客户端，其中包含了netty的处理管道列表（有序的）
     * 3. 初始化请求，这里我们用tcp的方式请求Http服务
     */
    private static void lbTcpRequest() throws URISyntaxException, ClientException, ClassNotFoundException {
        System.out.println("Load balance http request ----------------");
        ILoadBalancer loadBalancer = LoadBalancerBuilder
                .newBuilder()
                .withRule(new WeightedResponseTimeRule())
                .buildFixedServerListLoadBalancer(Arrays.asList(
                        new Server("atomicer.cn", 80),
                        new Server("baidu.com", 80),
                        new Server("zhihu.com", 443)
                ));

        URI uri = new URI("tcp://LB-APP");

        Object tcpRequestData = initCommonHttpRequest(uri);
        TcpRequest request = new TcpRequest
                .Builder(uri)
                .setData(tcpRequestData)
                .build();

        Initializer initializer = new DefaultPipelineInitializer(Arrays.asList(
                HttpClientCodec.class.getName(),
                HttpContentDecompressor.class.getName()
        ));
        AsyncLoadBalancingTcpClient client = new AsyncLoadBalancingTcpClient(loadBalancer, null, initializer);
        try {

            for (int i = 0; i < 10; i++) {
                // Note: this is necessary for sharing ByteBuf (ReferenceCounted): io.netty.handler.codec.http.HttpRequest
                // http://netty.io/wiki/reference-counted-objects.html#wiki-h2-1
                ReferenceCountUtil.retain(tcpRequestData);

                AsyncTcpResponse response = client.executeWithLoadBalancer(request);
                response.await();
                System.out.println(String.format("\n--------- %s ----------", response.getRequestedURI()));
                System.out.println(response.isSuccess());

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            client.shutdown();
        }

    }

    private static io.netty.handler.codec.http.HttpRequest initCommonHttpRequest(URI uri) {

        io.netty.handler.codec.http.HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        request.headers().set("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        request.headers().set("User-Agent", "/netty/5.x");
        request.headers().set("Accept-Encoding", "gzip, deflate, br");
        return request;
    }
}
