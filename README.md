### plum-mesh-agent
[![Build Status](https://travis-ci.org/suniper/plum-mesh-agent.svg?branch=master)](https://travis-ci.org/suniper/plum-mesh-agent)  [![Coverage Status](https://coveralls.io/repos/github/suniper/plum-mesh-agent/badge.svg?branch=master)](https://coveralls.io/github/suniper/plum-mesh-agent?branch=master)

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
```

* 简单的服务发现/负载均衡
```java
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
```

#### 注册中心

理论上所有的KV存储都可以作为注册中心，只需实现KVStore接口中的所有方法，pma目前提供了对以下服务的支持：

* Zookeeper
* etcd

> 将来可以陆续增加更多的注册中心的支持，比如 redis、mongo等

#### Ribbon自动配置
pma 集成了Ribbon的功能，如果需要对ribbon的参数做一些调整，只需修改配置文件（client name 为 `plum`），例如：
```properties
# Max number of retries on the same server (excluding the first try)
plum.ribbon.MaxAutoRetries=3
# Max number of next servers to retry (excluding the first server)
plum.ribbon.MaxAutoRetriesNextServer=1
# Whether all operations can be retried for this client
plum.ribbon.OkToRetryOnAllOperations=true
# Connect timeout used by Apache HttpClient
plum.ribbon.ConnectTimeout=3000
# Read timeout used by Apache HttpClient
plum.ribbon.ReadTimeout=3000
```

若pma的初始化参数没有显式地注明初始化netty或okhttp客户端，则会通过ribbon配置初始化负载均衡客户端，具体可见ribbon的[文档](https://github.com/Netflix/ribbon/wiki/Programmers-Guide)

#### Netty自动配置，池化连接
作为高性能的网络通信框架，netty在不少项目中都有引入，这里同样提供了对netty的支持。Netty客户端同样可以通过配置初始化，配置示例如下：
```properties
# EventLoopGroup的类型，默认为NioEventLoopGroup
plum.tcp.groupEventType=io.netty.channel.nio.NioEventLoopGroup
# socket通道类型默认为NioSocketChannel
plum.tcp.socketChannelType=io.netty.channel.socket.nio.NioSocketChannel
# group工作线程数，默认为4
plum.tcp.workers=4
# 连接池最大的连接数，默认为20
plum.tcp.maxPoolConn=30
# channel pipeline从头到尾将要添加的handler
plum.tcp.channelPipelines=io.netty.handler.codec.http.HttpClientCodec, io.netty.handler.codec.http.HttpContentDecompressor, SimpleHttpHandler
```
只需在`AppParameters`中将`autoTcpClient`设为`true`
（命令行中`plum.auto.tcp=true`），pma将自动初始化基于netty的负载均衡客户端

#### OkHttp客户端
只需在`AppParameters`中将`okHttpClient`设为`true` （命令行中`plum.auto.http=true`），pma的客户端即可初始化为基于OkHttp的负载均衡客户端

#### 使用负载均衡

`plum-mesh-transport`模块提供的tcp和http负载均衡可以单独使用，以下是一个简单的负载均衡示例：

```java
ILoadBalancer loadBalancer = LoadBalancerBuilder
                .newBuilder()
                .withRule(new RandomRule())
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
    // 通过打印的结果我们可以看到列表中的三个host被随机请求
    HttpResponse response = httpClient.executeWithLoadBalancer(request);
    System.out.println(String.format("\n--------- %s ----------", response.getRequestedURI()));
    System.out.println(response.getStatus());
    System.out.println(response.getPayload());
}
```