package cn.suniper.mesh.discovery.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class ApplicationTest {
    @Test
    public void setServerGroup() throws Exception {
        Application application = new Application();
        String group = "g";
        application.setServerGroup(group);
        assertEquals(group, application.getServerGroup());
    }

    @Test
    public void setRegistryUrlList() throws Exception {
        Application application = new Application();
        List<String> registryUrlList = Arrays.asList("u1", "u2", "u3");
        application.setRegistryUrlList(registryUrlList);
        assertEquals(registryUrlList, application.getRegistryUrlList());
    }

    @Test
    public void setProviderInfo() throws Exception {
        List<String> registryUrlList = Arrays.asList("u1", "u2", "u3");
        String group = "g";

        Application application = new Application(registryUrlList, group);
        assertNull(application.getProviderInfo());
        assertEquals(registryUrlList, application.getRegistryUrlList());
        assertEquals(group, application.getServerGroup());

        ProviderInfo providerInfo = new ProviderInfo();
        application.setProviderInfo(providerInfo);
        assertEquals(providerInfo, application.getProviderInfo());
    }

    @Test
    public void testCreate() {
        List<String> registryUrlList = Arrays.asList("u1", "u2", "u3");
        String group = "g";

        Application application1 = new Application(registryUrlList, group);
        Application application2 = new Application(registryUrlList, new ProviderInfo(), group);
        assertEquals(application1.getRegistryUrlList(), application2.getRegistryUrlList());
        assertEquals(application1.getServerGroup(), application2.getServerGroup());
    }

}