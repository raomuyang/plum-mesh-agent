package cn.suniper.mesh.discovery.annotation;

import cn.suniper.mesh.discovery.commons.KvSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Note that this is a service provider, then PlumApplication will
 * automatically register the current service in the Registry.
 *
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AsConsumer {
    KvSource.Provider kv() default KvSource.Provider.AUTO;
}
