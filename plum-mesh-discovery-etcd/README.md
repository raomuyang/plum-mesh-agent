### Etcd KVStore

> 基于`com.coreos.jetcd 0.0.2`实现, 须确保使用的`com.coreos.jetcd`对此版本的兼容

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
        <artifactId>plum-mesh-discovery-etcd</artifactId>
        <version>${plum-mesh-version}</version>
    </dependency>
</dependencies>

```