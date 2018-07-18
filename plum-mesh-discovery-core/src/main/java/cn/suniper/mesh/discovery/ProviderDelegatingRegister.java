package cn.suniper.mesh.discovery;

import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.discovery.model.ProviderInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;

/**
 * @author Rao Mengnan
 *         on 2018/6/11.
 */
public class ProviderDelegatingRegister {
    private Log log = LogFactory.getLog(getClass());

    private KVStore store;
    private Application application;


    public ProviderDelegatingRegister(KVStore store, Application application) {
        if (application == null) throw new IllegalArgumentException("application must be not null");
        this.application = application;
        this.store = store;
    }

    public void register() throws Exception {
        String parentNode = String.join("/", Constants.STORE_ROOT, application.getServerGroup());
        ProviderInfo providerInfo = Optional.ofNullable(application.getProviderInfo())
                .orElse(new ProviderInfo());

        String storeValue = String.format("%s/%s/%s",
                providerInfo.getIp(),
                providerInfo.getPort(),
                providerInfo.getWeight());

        String storeKey = String.join("/", parentNode, providerInfo.getName());

        // check and create
        store.createParentNode(parentNode);
        long reversion = store.put(storeKey, storeValue, true);
        log.info(String.format("registered server: `%s` in node: `%s`, reversion: %s",
                storeKey, storeValue, reversion));
    }


}
