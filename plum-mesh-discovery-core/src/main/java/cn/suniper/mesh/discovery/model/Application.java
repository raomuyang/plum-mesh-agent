package cn.suniper.mesh.discovery.model;

import java.util.List;

/**
 * The information of service-discovery application.
 * <p>
 * Parameters list
 * <ul>
 * <li>registryUrlList: specify the URLs to register or discovery services</li>
 * <li>serverGroup:  the services will be registered to a registry with "serverGroup", and a client will find services from the registry by "serverGroup"</li>
 * <li>providerInfo: see {@link ProviderInfo} </li>
 * </ul>
 * <p>
 * What do we need to provide
 * <ul>
 * <li>As a discovery-enabled service: you need provide full information that includes the {@link ProviderInfo} which is necessary for register</li>
 * <li>As a client: you just need to provide the {@link Application#registryUrlList} and {@link Application#serverGroup}</li>
 * </ul>
 *
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public class Application {
    private List<String> registryUrlList;

    private ProviderInfo providerInfo;

    private String serverGroup;

    public Application() {
    }

    public Application(List<String> registryUrlList, String serverGroup) {
        this.registryUrlList = registryUrlList;
        this.serverGroup = serverGroup;
    }

    public Application(List<String> registryUrlList, ProviderInfo providerInfo, String serverGroup) {
        this.registryUrlList = registryUrlList;
        this.providerInfo = providerInfo;
        this.serverGroup = serverGroup;
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public void setServerGroup(String serverGroup) {
        this.serverGroup = serverGroup;
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
