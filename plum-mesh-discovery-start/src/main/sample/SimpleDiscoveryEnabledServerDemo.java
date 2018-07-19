import cn.suniper.mesh.discovery.PlumApplication;
import cn.suniper.mesh.discovery.annotation.AsProvider;
import cn.suniper.mesh.discovery.annotation.KvStoreBean;
import cn.suniper.mesh.discovery.commons.ConfigManager;
import cn.suniper.mesh.discovery.commons.ConnAutoInitializer;
import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * 假装这个类会开放8080端口提供服务
 */
@AsProvider
public class SimpleDiscoveryEnabledServerDemo {

    public static void main(String[] args) {
        Object primary = new SimpleDiscoveryEnabledServerDemo();

        // 设置服务名、开放的端口，所属的server group（用于服务发现）
        ProviderInfo providerInfo = new ProviderInfo("test-provider-1", 8080);
        Application application = new Application(null, providerInfo, "demo");

        // 初始化ConfigManager实例
        ConfigManager configManager = ConfigManager.newBuilder().withAppInfo(application).build();

        try {
            PlumApplication.launch(primary, configManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // do sth start the server
    }

    @KvStoreBean
    public ZooKeeper getZkClient() throws IOException, InterruptedException {
        return ConnAutoInitializer.zkConnection(
                "192.168.1.111:2181,192.168.1.112:2181", 1000);
    }
}
