import cn.suniper.mesh.discovery.PlumApplication;
import cn.suniper.mesh.discovery.RegisteredServerDynamicList;
import cn.suniper.mesh.discovery.annotation.AsConsumer;
import cn.suniper.mesh.discovery.annotation.KvStoreBean;
import cn.suniper.mesh.discovery.cli.AppParameters;
import cn.suniper.mesh.discovery.cli.PlumContext;
import cn.suniper.mesh.discovery.commons.ConfigManager;
import cn.suniper.mesh.discovery.commons.ConnAutoInitializer;
import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.transport.http.LoadBalancingHttpClient;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * @author Rao Mengnan
 *         on 2018/7/20.
 */
@AsConsumer
public class SimpleClientSideLoadBalance {

    public static void main(String[] args) {
        Object primary = new SimpleClientSideLoadBalance();
        AppParameters parameters = AppParameters.newBuilder().enableOkHttp().build();
        // 指定初始化使用动态列表的okhttp客户端
        Application application = new Application(null, null, "demo");
        ConfigManager configManager = ConfigManager.newBuilder().withAppInfo(application).build();
        try {
            PlumContext context = PlumApplication.launch(primary, parameters, configManager);

            // 获取负载均衡客户端
            LoadBalancingHttpClient client = (LoadBalancingHttpClient) context.getClient();

            // 创建并发送请求
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .verb(HttpRequest.Verb.GET)
                    .uri("https://LB-APP/test:8080")
                    .build();
            HttpResponse response = client.executeWithLoadBalancer(request, null);
            System.out.println(response.getStatus());

            // 获取可用服务列表
            RegisteredServerDynamicList list = context.getDynamicServerList();
            System.out.println(list.getCachedListOfServers());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KvStoreBean
    public ZooKeeper getZkClient() throws IOException, InterruptedException {
        return ConnAutoInitializer.zkConnection(
                "192.168.1.111:2181,192.168.1.112:2181", 1000);
    }
}
