package cn.suniper.mesh.discovery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method annotation `KvStoreBean` (optional), by labeling on a no-argument method,
 * tells PlumApplication that we have our own method to provide a
 * {@link cn.suniper.mesh.discovery.KVStore} bean ({@code jetcd.Client}, {@code ZooKeeper}, etc.). If not found
 * When this annotation or method call fails, PlumApplication will attempt to automatically initialize a simple
 * KvStore instance through configuration.
 *
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KvStoreBean {
}
