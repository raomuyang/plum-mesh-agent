package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.ProviderInfo;
import com.netflix.loadbalancer.Server;

/**
 * @author Rao Mengnan
 *         on 2018/6/13.
 */
public class RegisteredServer extends Server {

    private ProviderInfo providerInfo;
    private MetaInfo metaInfo;

    public RegisteredServer(final String appName, ProviderInfo info) {
        super(info.getIp(), info.getPort());
        this.providerInfo = info;
        this.metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return null;
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }
}
