package cn.suniper.mesh.transport;

/**
 * {@link Constants#DEFAULT_VIP_ADDRESS}:
 * <p>
 * About vipAddress:
 * <p>
 * See <a href="https://github.com/Netflix/ribbon/blob/master/ribbon-core/src/main/java/com/netflix/client/SimpleVipAddressResolver.java" target="_blank"> SimpleVipAddressResolver </a>
 * <p>
 * {@link com.netflix.loadbalancer.LoadBalancerContext#isVipRecognized(String)}
 * <p>
 * A "VipAddress" in Ribbon terminology is a logical name used for a target
 * server farm. This class helps interpret and resolve a "macro" and obtain a
 * finalized vipAddress.
 *
 * @author Rao Mengnan
 *         on 2018/7/4.
 */
public interface Constants {

    // Default vipAddress for ribbon load balance
    String DEFAULT_VIP_ADDRESS = "LB-APP";
    String CONFIG_PREFIX = "plum.tcp";
}
