import cn.suniper.mesh.transport.tcp.AsyncLoadBalancingTcpClient;
import cn.suniper.mesh.transport.tcp.AsyncTcpResponse;
import cn.suniper.mesh.transport.tcp.ConnectionPoolManager;
import cn.suniper.mesh.transport.tcp.TcpRequest;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * 通过配置文件初始化Tcp客户端，使用tcp客户端发送http请求
 *
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public class ClientInitFromPropertiesFileSample {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClientInitFromPropertiesFileSample.class.getResourceAsStream("/config.properties"));
        ConnectionPoolManager manager = ConnectionPoolManager.initFromClientProperties(properties);

        try {

            AsyncLoadBalancingTcpClient client = new AsyncLoadBalancingTcpClient(null, null, manager);
            URI uri = new URI("http://baidu.com:80");
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            request.headers().set("Host", "www.baidu.com");
            request.headers().set("User-Agent", "/netty/5.x");
            request.headers().set("Accept-Encoding", "gzip, deflate, br");

            TcpRequest tcpRequest = new TcpRequest.Builder(uri).setData(request).build();

            AsyncTcpResponse response = client.execute(tcpRequest, null);

            response.await();

            ChannelFuture ch =  response.getPayload();
            System.out.println("is done: " + ch.isDone());
            response.close();

            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.shutdown();
        }

    }
}
