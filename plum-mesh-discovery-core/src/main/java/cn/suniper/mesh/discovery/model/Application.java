package cn.suniper.mesh.discovery.model;

import java.util.List;

/**
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class Application {
    private List<String> registryUrlList;

    private ProviderInfo providerInfo;

    private String name;

    private boolean asProvider;

    public Application() {
    }

    public Application(List<String> registryUrlList, ProviderInfo providerInfo, String name) {
        this.registryUrlList = registryUrlList;
        this.providerInfo = providerInfo;
        this.name = name;
    }

    public boolean isAsProvider() {
        return asProvider;
    }

    public void setAsProvider(boolean asProvider) {
        this.asProvider = asProvider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRegistryUrlList() {
        return registryUrlList;
    }

    public void setRegistryUrlList(List<String> registryUrlList) {
        this.registryUrlList = registryUrlList;
    }

    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(ProviderInfo providerInfo) {
        this.providerInfo = providerInfo;
    }


}
