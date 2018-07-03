package cn.suniper.mesh.discovery.provider;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Rao Mengnan
 *         on 2018/5/11.
 */
public class ZKConnection {

    public static ZooKeeper connect(String hosts, int timeout) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(hosts, timeout, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                latch.countDown();
            }
        });
        latch.await();
        return zooKeeper;
    }
}
