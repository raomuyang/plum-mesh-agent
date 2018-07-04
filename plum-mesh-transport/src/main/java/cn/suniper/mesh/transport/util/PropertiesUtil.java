package cn.suniper.mesh.transport.util;

import cn.suniper.mesh.transport.tcp.NettyClientProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public class PropertiesUtil {
    private static Log log = LogFactory.getLog(PropertiesUtil.class);

    public static NettyClientProperties getClientPropFromProperties(Properties properties) {
        NettyClientProperties tcpClientProperties = new NettyClientProperties();
        BeanInfo beanInfo;

        try {
            beanInfo = Introspector.getBeanInfo(NettyClientProperties.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            if (descriptor.getName().equals("class")) continue;
            if (properties.get(descriptor.getName()) == null) continue;

            Object v;
            if (descriptor.getPropertyType() == List.class) {
                String listStr = properties.getProperty(descriptor.getName());
                String[] values = listStr.split(",");
                v = Arrays.asList(values);
            } else if (descriptor.getPropertyType() == int.class) {
                Object intVal = properties.computeIfAbsent(descriptor.getName(), s -> "0");
                v = Integer.valueOf(String.valueOf(intVal));
            } else {
                v = properties.get(descriptor.getName());
            }
            Method setter = descriptor.getWriteMethod();
            try {
                setter.invoke(tcpClientProperties, v);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.debug(e);
                throw new IllegalArgumentException("illegal property value");
            }
        }
        return tcpClientProperties;
    }

}
