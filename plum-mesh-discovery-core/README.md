### plum-mesh-discovery-core

  有些人偏爱`etcd`，也有人更喜欢`zookeeper`，抑或是其它类型的分布式kv存储框架，总之大家总是希望基于自己的
技术栈自由选择存储介质
  所以discovery-core没有指定使用何种存储服务作为实现服务发现的介质，只定义了一系列接口，并基于接口封装了一系列
行为，其中包括：
* 服务注册
* 动态服务列表
* 动态服务列表更新

#### 服务注册

使用`ProviderDelegatingRegister`注册当前服务，只需将实现的`KVStore`实例和`Application`信息传入：

1. 无论使用何种KV Store，注册服务的根节点为 `/config/suniper`
2. Register注册服务时，会在根节点下创建以`{ServerGroup}`为名的节点 => `/config/suniper/{ServerGroup}`
3. 相关的服务信息会存储在子节点中： key: `/config/suniper/{AppName}` <=>  value: `ip/port/weight` 
4. 服务信息存储的节点会注册为临时节点，Register会以守护线程的方式保持连接，所以所有的KV Store必须满足客户端断开连接一段时间之内会节点会自动失效

#### 动态服务列表/服务发现

##### 原理
* 通过KV Store获取可用的服务信息初始化可用的服务列表；
* 监听KV Store中节点的变化，从而实现可用服务列表的动态更新

藉此我们可以实现动态列表`RegisteredServerDynamicList`和列表更新器`RegistryServerListUpdater`。

##### RegisteredServerDynamicList
通过`kvStore`和`ServerGroupName`创建的动态列表，会使用`ServerGroupName`找到服务列表的根节点，然后从`kvStore`中获取所有可用的子节点：
* 获取初始的服务列表： `RegisteredServerDynamicList.getInitialListOfServers`
* 更新并获取服务列表：`RegisteredServerDynamicList.getUpdatedListOfServers`
* 获取缓存的服务列表：`RegisteredServerDynamicList.getCachedListOfServers`

##### RegistryServerListUpdater
* 服务列表更新器，通过kvStore对子节点的监控，实时更新服务列表
 * RegistryServerListUpdater绑定一个providerInfoMap，这个map必须是线程安全的。
 * 每个updater实例启动更新时，会通过watcher监听/`conf/suniper/{serverGroup}`下的子节点。
 * `RegistryServerListUpdater.start(UpdateAction)`的调用是幂等的。
 * 默认情况下，每个updater启动後会运行在守护线程中，当然也可以通过`RegistryServerListUpdater.setExecutorService(ExecutorService)`自定义运行时的线程
 * 默认情况下出错不会做任何处理，如有需要可以通过`RegistryServerListUpdater.setOnError(Consumer)` 设置
