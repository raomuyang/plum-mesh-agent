### Zookeeper KVStore

> 基于`org.apache.zookeeper 3.4.12`实现, 须确保使用的`org.apache.zookeeper`对此版本的兼容

实现基本的KvStore操作，包含`put` `get` `watch_children`等操作，配合 `plum-mesh-discovery-core使用`：

```xml
<dependencies>
    <dependency>
        <groupId>cn.suniper</groupId>
        <artifactId>plum-mesh-discovery-core</artifactId>
        <version>${plum-mesh-version}</version>
    </dependency>
            
    <dependency>
        <groupId>cn.suniper</groupId>
        <artifactId>plum-mesh-discovery-zk</artifactId>
        <version>${plum-mesh-version}</version>
    </dependency>
</dependencies>

```