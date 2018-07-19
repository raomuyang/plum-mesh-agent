### plum-mesh-agent

pma是便捷高效的服务发现和网络通信中间件，封装了ribbon、netty、http等工具，可以快速地实现服务注册和负载均衡。

#### 模块
* plum-mesh-discovery-core Service discovery核心模块
* plum-mesh-discovery-etcd 支持基于etcd的服务发现
* plum-mesh-discovery-zk 支持基于Zookeeper的服务发现
* plum-mesh-transport netty、okhttp的
* plum-mesh-discovery-start

#### 快速开始

以Zookeeper为例，使用 `plum-mesh-discovery-start` 快速开始一个pma程序，`plum-mesh-discovery-start`可以通过classpath中添加的依赖决定自己的行为：

```xml
<!-- 添加quick start模块依赖 -->
<dependency>
    <groupId>cn.suniper</groupId>
    <artifactId>plum-mesh-discovery-start</artifactId>
    <version>${version}</version>
</dependency>

<!-- 添加指定的KV store类型为zookeeper -->
<dependency>
    <groupId>cn.suniper</groupId>
    <artifactId>plum-mesh-discovery-zk</artifactId>
    <version>${version}</version>
</dependency>

<!-- 提供zookeeper版本 -->
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>${zk.version}</version>
</dependency>
```

pma不会指定zookeeper/etcd库的版本，所以需要添加相应的依赖，除此之外，用户对程序的初始化过程都是无感知的。

* 简单的服务注册（假设这个服务可以在8080端口上提供一个服务）

```java

@AsProvider
public class SimpleDiscoveryEnabledServerDemo {

    public static void main(String[] args) {
        Object primary = new SimpleDiscoveryEnabledServerDemo();
        AppParameters parameters = AppParameters.newBuilder().build();

        ProviderInfo providerInfo = new ProviderInfo("test-provider-1", 8080);

        Application application = new Application(null, providerInfo, "demo");
        ConfigManager configManager = ConfigManager.newBuilder().withAppInfo(application).build();
        try {
            PlumApplication.launch(primary, parameters, configManager);
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
```


#### 注册中心

理论上所有的KV存储都可以作为注册中心，只需实现KVStore接口中的所有方法，pma默认提供了对以下服务的支持：
* Zookeeper
* etcd

> 将来可以陆续增加更多的注册中心的支持，比如 redis、mongo等

#### 