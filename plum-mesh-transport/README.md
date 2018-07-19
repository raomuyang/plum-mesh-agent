## plum-mesh-transport 

基于主流的通信框架（netty和okhttp），提供tcp和http通讯客户端，可单独使用也可用于负载均衡

### Tcp
netty是一个高性能的通讯框架，plum-mesh-transport基于netty实现的异步通信客户端，可快速启动一个典型的netty应用程序。

通过简单地配置channelPipeline，你可以快速地启动一个普通的netty客户端：
```java
Initializer initializer = new DefaultPipelineInitializer(Arrays.asList(
                HttpClientCodec.class.getName(),
                HttpContentDecompressor.class.getName(),
                "you.handler.XXXHandler"
        ));
AsyncLoadBalancingTcpClient client = new AsyncLoadBalancingTcpClient(null, null, initializer);
```

当然，在某些场景下，我们可能需要对多个服务创建大量可复用的连接，这个时候就要用到netty的连接池：

```java
List<String> channels = Arrays.asList(
                HttpClientCodec.class.getName(),
                HttpContentDecompressor.class.getName(),
                "you.handler.XXXHandler"
        );
Properties properties = new Properties();
// 你可以将它们写到配置文件中，从而避免这个繁琐的操作
properties.put(TransportConfigKey.GROUP_EVENT_TYPE.propName(), "io.netty.channel.nio.NioEventLoopGroup");
properties.put(TransportConfigKey.SOCKET_CHANNEL_TYPE.propName(), "io.netty.channel.socket.nio.NioSocketChannel");
properties.put(TransportConfigKey.WORKERS.propName(), 4);
properties.put(TransportConfigKey.MAX_POOL_CONN.propName(), 30);
properties.put(TransportConfigKey.CHANNEL_PIPELINES.propName(), String.join(",", channels));

ConnectionPoolManager manager = ConnectionPoolManager.initFromClientProperties(properties);
AsyncLoadBalancingTcpClient client = new AsyncLoadBalancingTcpClient(null, null, manager);
```

### Http
http客户端借助时下流行的okhttp模块实现，可以无需任何配置开箱即用：

```java
HttpRequest request = new HttpRequest.Builder()
                .verb(HttpRequest.Verb.GET)
                .uri("https://github.com")
                .build();
LoadBalancingHttpClient httpClient = new LoadBalancingHttpClient();
HttpResponse response = httpClient.execute(request, null);
```

### 负载均衡
ribbon是稳定的负载均衡框架，所有的http、tcp客户端均与之整合，可以直接配合ribbon使用
我们以http客户端为例实现一个简单的例子，对一个固定的服务列表进行负载均衡：
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

### 将来支持

* 服务代理