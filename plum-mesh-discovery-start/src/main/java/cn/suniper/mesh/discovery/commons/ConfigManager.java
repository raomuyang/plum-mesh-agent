package cn.suniper.mesh.discovery.commons;

import cn.suniper.mesh.discovery.annotation.CommonPropertyName;
import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import cn.suniper.mesh.transport.Constants;
import cn.suniper.mesh.transport.tcp.NettyClientProperties;
import cn.suniper.mesh.transport.util.PropertiesUtil;
import com.google.common.collect.Lists;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

/**
 * @author Rao Mengnan
 *         on 2018/7/6.
 */
public class ConfigManager {
    public static final String DEFAULT_CONFIG_NAME = "plum.properties";

    private static final String DELIMITER = ",";
    private Configuration cfgTool;

    private Application application;
    private NettyClientProperties nettyClientProperties;
    private IClientConfig ribbonClientConfig;
    private boolean hasTcpConfig;

    private ConfigManager() {

    }

    public static class Builder {
        ConfigManager configManager;
        private Builder() {
            configManager = new ConfigManager();
        }

        public Builder withAppInfo(Application appInfo) {
            configManager.application = appInfo;
            return this;
        }

        public Builder withRibbonClientConfig(IClientConfig ribbonClientConfig) {
            configManager.ribbonClientConfig = ribbonClientConfig;
            return this;
        }

        public ConfigManager build() {
            if (configManager.application == null)
                throw new IllegalStateException("Application information was not indefinite");
            if (configManager.ribbonClientConfig == null) {
                Properties properties = new Properties();
                try {
                    properties.load(ConfigManager.class.getResourceAsStream("/" + DEFAULT_CONFIG_NAME));
                    ConfigurationManager.loadProperties(properties);
                    configManager.ribbonClientConfig = new DefaultClientConfigImpl();
                    configManager.ribbonClientConfig.loadProperties(CommonPropertyName.RIBBON_CLIENT_NAME.propName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return configManager;
        }
    }

    public static ConfigManager loadProperties(String path) throws ConfigurationException {
        PropertiesBuilderParameters parameters = new Parameters().properties()
                .setFileName(path)
                .setEncoding("utf-8")
                .setListDelimiterHandler(new DefaultListDelimiterHandler(DELIMITER.charAt(0)));

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class).configure(parameters);
        ConfigManager manager = new ConfigManager();
        manager.cfgTool = builder.getConfiguration();
        manager.initFromProperties();

        return manager;
    }


    private void initFromProperties() {
        application = new Application();
        ProviderInfo providerInfo = new ProviderInfo();
        application.setProviderInfo(providerInfo);

        Properties properties = new Properties();
        cfgTool.getKeys().forEachRemaining(key -> {
            properties.put(key, cfgTool.getProperty(key));

            // 判断是否有netty相关的配置
            if (!hasTcpConfig && key.startsWith(Constants.CONFIG_PREFIX)) hasTcpConfig = true;

            if (!key.startsWith(CommonPropertyName.PREFIX.propName())) return;

            CommonPropertyName prop = CommonPropertyName.get(key);
            if (prop == null || prop == CommonPropertyName.PREFIX) return;

            Object value = safeGetValue(prop, key);
            try {
                switch (prop) {
                    case ENABLE_PROVIDER:
                    case APP_NAME:
                    case REGISTRY_LIST:
                        BeanUtils.setProperty(application, prop.fieldName(), value);
                        break;
                    default:
                        BeanUtils.setProperty(providerInfo, prop.fieldName(), value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        });

        if (application.isAsProvider()) {
            providerInfo.setName(application.getName());
        }

        ConfigurationManager.loadProperties(properties);
        ribbonClientConfig = new DefaultClientConfigImpl();
        ribbonClientConfig.loadProperties(CommonPropertyName.RIBBON_CLIENT_NAME.propName());

        nettyClientProperties = PropertiesUtil.getClientPropFromProperties(properties);
    }

    private Object safeGetValue(CommonPropertyName prop, String key) {
        if (prop.type() == List.class) {
            String str = String.valueOf(cfgTool.getProperty(key));
            if (str != null && str.split(DELIMITER).length == 1) {
                return Lists.newArrayList(str);
            }
        }
        return cfgTool.get(prop.type(), key);
    }

    public Application getApplication() {
        return application;
    }

    public NettyClientProperties getNettyClientProperties() {
        return nettyClientProperties;
    }

    public IClientConfig getRibbonClientConfig() {
        return ribbonClientConfig;
    }

    public boolean hasPlumTcpConfig() {
        return hasTcpConfig;
    }
}
