### Plum-Discovery-Quick-Start

  模块中封装了对`plum-mesh-discovery-etcd`和`plum-mesh-discovery-zk`的支持，通过反射和内省确定环境中是否提供相应的
依赖，并根据注解和配置文件快速初始化。

#### What does it do?

* 快速将当前服务注册到`etcd`/`zookeeper`
* 通过配置快速获取负载均衡客户端，实时监听`etcd`/`zookeeper`上注册的节点编号

#### 如何工作

##### 环境判断
  对于`plum-mesh-discovery-etcd`和`plum-mesh-discovery-zk`，模块在pom中将依赖设为`runtime`， `cn.suniper.mesh.discovery.commons.ConnAutoInitializer`会根据运行时环境中是否包含
依赖的路径判断应该初始化哪一种类型的`KV Store`。也可以通过`AsConsumer`和`AsProvider`中的`kv`参数指定作为Registry的KvStore类型。

##### 程序初始化
  用户可以使用任何对象作为基本入口初始化一个PlumApplication，有以下两种方式可以快速初始化一个PlumApplication:
* 命令行的方式初始化：`PlumApplication.launch(Object primary, String... args)` 。通过命令行的方式初始化时，
程序解析所有的字符串参数并初始化`AppParameters` `ConfigManager`等信息
* 自定义配置初始化：`launch(Object primary, AppParameters parameters, ConfigManager configManager)`
  
  无论使用何种方式初始化app，作为参数的`primary object`是必不可少的。上面提到任何Object都可以作为primary参数传入，PlumApplication会通过注解获取初始化信息。
在初始化时预期会得到以下信息：
* 类级注解 `AsProvider` 或 `AsConsumer`，这两个注解告诉PlumApplication我们希望初始化哪种类型的程序：
    * `AsProvider` 表示这是一个服务提供者，那么PlumApplication会自动将当前服务在Registry中注册
    * `AsConsumer` 表示这是一个服务消费者，那么PlumApplication会通过Registry初始化一个动态服务列表，并根据配置初始化相应的负载均衡客户端
* 方法注解`KvStoreBean`（optional），通过在一个无参的方法上标注，告诉PlumApplication我们有自己的方法提供一个`KVStore`的bean（`jetcd.Client`、`ZooKeeper`等）。如果找不到
这个注解或者方法调用失败时，PlumApplication会尝试通过配置自动初始化一个简单的KvStore实例
```java
import cn.suniper.mesh.discovery.annotation.*;

@AsProvider
class SamplePrimaryClassAsProvider {
    
    @KvStoreBean
    public org.apache.zookeeper.ZooKeeper getZkClient() {
        // do any thing
        return obj;
    }
}

@AsConsumer
class SamplePrimaryClassAsProvider {
    
    @KvStoreBean
    public com.coreos.jetcd.Client getEtcdClient() {
        // do any thing
        return obj;
    }
}
```
  初始化完成后即可获得`PlumContext`信息，记录了动态服务列表、负载均衡客户端等信息，如果是以`AsConsumer`运行，可以从context中获取客户端用于其它操作。
  
##### 从配置初始化KvStore对应的的客户端

当无需从primary object中获取定制化的KvStore客户端，可以直接通过配置简单地初始化相应的客户端：
```properties
plum.registries=192.168.1.111:2171,192.168.1.112:2171,192.168.1.113:2171
```
或者直接设置`Application`的RegistryList：
```java
ConfigManager configManager = ConfigManager.newBuilder().build();
configManager.getApplication().setRegistryUrlList(
              java.util.Arrays.asList(
                      "192.168.1.111:2171", 
                      "192.168.1.112:2171",
                      "192.168.1.113:2171"
              )
      );
```

#### Ribbon自动配置
plum-mesh-agent 集成了Ribbon的功能，如果需要对ribbon的参数做一些调整，只需修改配置文件（client name 为 `plum`），例如：
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

#### 可插拔的Netty客户端自动配置
  `plum-mesh-transport`作为组件中的一部分，netty客户端同样可以通过配置初始化，只需在`AppParameters`中将`autoTcpClient`设为`true`
（命令行中`plum.auto.tcp=true`）
配置示例如下：
```properties
plum.tcp.groupEventType=io.netty.channel.nio.NioEventLoopGroup
plum.tcp.socketChannelType=io.netty.channel.socket.nio.NioSocketChannel
plum.tcp.workers=4
plum.tcp.maxPoolConn=30
plum.tcp.channelPipelines=io.netty.handler.codec.http.HttpClientCodec, io.netty.handler.codec.http.HttpContentDecompressor, SimpleHttpHandler
```
#### OkHttp客户端
在`AppParameters`中将`okHttpClient`设为`true` （命令行中`plum.auto.http=true`）