package cn.suniper.mesh.discovery;


import cn.suniper.mesh.discovery.annotation.AsConsumer;
import cn.suniper.mesh.discovery.annotation.AsProvider;
import cn.suniper.mesh.discovery.annotation.ClientTypeEnum;
import cn.suniper.mesh.discovery.annotation.KvStoreBean;
import cn.suniper.mesh.discovery.cli.AppParameters;
import cn.suniper.mesh.discovery.cli.PlumContext;
import cn.suniper.mesh.discovery.commons.ConfigManager;
import cn.suniper.mesh.discovery.commons.KvSource;
import cn.suniper.mesh.discovery.model.Application;
import cn.suniper.mesh.transport.http.LoadBalancingHttpClient;
import cn.suniper.mesh.transport.tcp.AsyncLoadBalancingTcpClient;
import cn.suniper.mesh.transport.tcp.ConnectionPoolManager;
import com.netflix.client.ClientFactory;
import com.netflix.client.IClient;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.CmdLineException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
public class PlumApplication {

    private static Log log = LogFactory.getLog(PlumApplication.class);

    public static PlumContext launch(Object primary, String... args) throws Exception {
        try {
            AppParameters parameters = AppParameters.parser(args);

            String path;
            if (parameters.getConfigPath() == null) {
                path = PlumApplication.class.getResource("/" + ConfigManager.DEFAULT_CONFIG_NAME).getPath();
            } else {
                path = parameters.getConfigPath();
            }
            ConfigManager configManager;
            try {
                configManager = ConfigManager.loadProperties(path);
            } catch (ConfigurationException e) {
                log.error(e.getMessage());
                throw new IllegalStateException(e);
            }

            return launch(primary, parameters, configManager);

        } catch (CmdLineException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    public static PlumContext launch(Object primary, AppParameters parameters, ConfigManager configManager) throws Exception {
        PlumContext context = initContext(primary);
        context
                .setConfigManager(configManager)
                .initPlumApp();
        if (!context.isAsServiceProvider()) {
            checkAndInitClient(context, parameters, configManager);
        } else {
            registerProvider(context);
        }
        return context;
    }

    /**
     * 向注册中心注册服务，保持与注册中心的连接直到主线程退出
     *
     * @param context 上下文信息
     * @throws Exception 通信等异常
     */
    private static void registerProvider(PlumContext context) throws Exception {
        ProviderDelegatingRegister register = context.getRegisterAgent();
        register.register();
    }

    /**
     * 检查并初始化客户端
     *
     * @param context       上下文信息，需获取{@link com.netflix.loadbalancer.ServerList}
     * @param parameters    命令行参数，确定客户端类型
     * @param configManager 通过configManager获取{@link cn.suniper.mesh.transport.tcp.NettyClientProperties}等信息
     */
    private static void checkAndInitClient(PlumContext context, AppParameters parameters, ConfigManager configManager) {

        IClientConfig iClientConfig = Optional
                .ofNullable(configManager.getRibbonClientConfig())
                .orElse(new DefaultClientConfigImpl());

        ILoadBalancer loadBalancer = LoadBalancerBuilder.<RegisteredServer>newBuilder()
                .withClientConfig(iClientConfig)
                .withDynamicServerList(context.getDynamicServerList())
                .buildDynamicServerListLoadBalancer();

        ClientTypeEnum clientType;
        IClient client;
        if (parameters.isAutoTcpClient()) {
            client = new LoadBalancingHttpClient(loadBalancer, iClientConfig);
            clientType = ClientTypeEnum.OKHTTP;
        } else if (parameters.isAutoTcpClient() || configManager.hasPlumTcpConfig()) {
            try {
                ConnectionPoolManager poolManager = ConnectionPoolManager.initFromClientProperties(
                        configManager.getNettyClientProperties());
                client = new AsyncLoadBalancingTcpClient(loadBalancer, iClientConfig, poolManager);
                clientType = ClientTypeEnum.ASYNC_TCP;
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
                throw new IllegalArgumentException("Init channel pipeline failed", e);
            }
        } else {
            client = ClientFactory.getNamedClient(iClientConfig.getClientName());
            clientType = ClientTypeEnum.DEFAULT;
        }
        context.putClient(clientType, client);
    }

    /**
     * 通过 primaryBean 初始化上下文信息，确定使用的注册中心类型及app的模式 （provider/consumer）
     * @param primary a bean that include the {@link AsProvider} annotation or {@link AsConsumer} annotation,
     *                if KvStoreBean was provided, the getter must be annotated by {@link KvStoreBean}
     * @return 初始化后的上下文信息
     */
    private static PlumContext initContext(Object primary) {
        KvSource.Provider kvType = null;
        boolean isProviderMode = false;

        Class<?> primaryClass = primary.getClass();


        AsProvider asProvider = primaryClass.getAnnotation(AsProvider.class);
        if (asProvider != null) {
            kvType = asProvider.kv();
            isProviderMode = true;
        }

        AsConsumer asConsumer = primaryClass.getAnnotation(AsConsumer.class);
        if (asConsumer != null) {
            kvType = asConsumer.kv();
            if (isProviderMode) {
                throw new IllegalArgumentException("Annotation conflict: AsConsumer cannot be used with AsProvider");
            }
        }

        Object kvStoreSource = getKvStoreBean(primary);
        PlumContext context = new PlumContext(isProviderMode, kvType);
        context.setKvStoreSource(kvStoreSource);

        return context;
    }

    /**
     * 通过入口bean获取kvStore相关的客户端连接
     * @param primary 入口
     * @return 返回kvStore 连接的实例
     */
    private static Object getKvStoreBean(Object primary) {
        Class<?> primaryClass = primary.getClass();
        Method[] methods = primaryClass.getDeclaredMethods();
        for (Method m : methods) {
            KvStoreBean kvStoreBean = m.getAnnotation(KvStoreBean.class);
            if (kvStoreBean != null) {
                try {
                    m.setAccessible(true);
                    return m.invoke(primary);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("The kv store cannot be initialized: " + e.getMessage());
                    log.debug(e);
                }
            }

        }
        return null;
    }

}
