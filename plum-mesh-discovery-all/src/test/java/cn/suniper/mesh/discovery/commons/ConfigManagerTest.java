package cn.suniper.mesh.discovery.commons;

import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import cn.suniper.mesh.transport.tcp.NettyClientProperties;
import cn.suniper.mesh.transport.util.PropertiesUtil;
import com.netflix.client.config.IClientConfig;
import org.junit.*;

import java.io.*;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/6.
 */
public class ConfigManagerTest {
    private String configPath;
    private Properties properties;

    @Before
    public void before() throws IOException {
        configPath = ConfigManagerTest.class.getResource("/test.properties").getFile();
        properties = new Properties();
        try (InputStream in = new FileInputStream(new File(configPath))){
            properties.load(in);
        }

    }

    @org.junit.Test
    public void testLoadProperties() throws Exception {
        ConfigManager manager = ConfigManager.loadProperties(configPath);

        NettyClientProperties nettyProps = manager.getNettyClientProperties();
        assertEquals(PropertiesUtil.getClientPropFromProperties(properties), nettyProps);
        assertEquals(3, nettyProps.getChannelPipelines().size());

        IClientConfig iClientConfig = manager.getRibbonClientConfig();
        assertEquals("plum", iClientConfig.getClientName());

        Application application = manager.getApplication();
        assertEquals("test", application.getName());
        assertTrue(application.isAsProvider());
        assertEquals(1, application.getRegistryUrlList().size());

        ProviderInfo providerInfo = application.getProviderInfo();
        assertEquals("test", providerInfo.getName());
        assertNull(providerInfo.getIp());
        assertEquals(8080, providerInfo.getPort());
        assertEquals(0, providerInfo.getVersion());
        assertEquals(1, providerInfo.getWeight());

    }

}