package cn.suniper.mesh.transport.http;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.RequestSpecificRetryHandler;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.loadbalancer.ILoadBalancer;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

/**
 * @author Rao Mengnan
 *         on 2018/6/14.
 */
public class LoadBalancingHttpClient
        extends AbstractLoadBalancerAwareClient<HttpRequest, HttpResponse> {

    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final int DEFAULT_READ_TIMEOUT = 1000;

    private IClientConfig icc;

    public LoadBalancingHttpClient() {
        this(null);
    }

    public LoadBalancingHttpClient(ILoadBalancer lb) {
        this(lb, null);
    }

    public LoadBalancingHttpClient(ILoadBalancer lb, IClientConfig clientConfig) {
        super(lb, clientConfig);
        this.icc = clientConfig;
    }

    @Override
    public RequestSpecificRetryHandler getRequestSpecificRetryHandler(HttpRequest request, IClientConfig requestConfig) {
        if (!request.isRetriable()) {
            return new RequestSpecificRetryHandler(false, false, this.getRetryHandler(), requestConfig);
        }
        if (this.icc.get(CommonClientConfigKey.OkToRetryOnAllOperations, false)) {
            return new RequestSpecificRetryHandler(true, true, this.getRetryHandler(), requestConfig);
        }
        // 非get请求不能重试
        if (request.getVerb() != HttpRequest.Verb.GET) {
            return new RequestSpecificRetryHandler(true, false, this.getRetryHandler(), requestConfig);
        } else {
            return new RequestSpecificRetryHandler(true, true, this.getRetryHandler(), requestConfig);
        }
    }

    @Override
    public HttpResponse execute(HttpRequest request, IClientConfig requestConfig) throws Exception {
        IClientConfig overrideConfig = requestConfig == null ? icc : requestConfig;
        if (overrideConfig == null) overrideConfig = new DefaultClientConfigImpl();
        long readTimeout = overrideConfig.get(CommonClientConfigKey.ReadTimeout, DEFAULT_READ_TIMEOUT);
        long connTimeout = overrideConfig.get(CommonClientConfigKey.ConnectTimeout, DEFAULT_CONNECT_TIMEOUT);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .writeTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connTimeout, TimeUnit.MILLISECONDS)
                .build();

        String type = request.getHttpHeaders().getFirstValue("Content-Type");

        RequestBody body = null;
        if (request.getEntity() != null) {
            body = RequestBody.create(MediaType.parse(type), (byte[]) request.getEntity());
        }

        Headers.Builder headersBuilder = new Headers.Builder();
        request.getHttpHeaders().getAllHeaders().forEach(e -> headersBuilder.add(e.getKey(), e.getValue()));

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(request.getUri().getScheme())
                .host(request.getUri().getHost())
                .port(request.getUri().getPort())
                .addPathSegment(request.getUri().getPath())
                .query(request.getUri().getQuery());
        request.getQueryParams().entrySet().forEach(e -> {
            for (String v : e.getValue()) {
                urlBuilder.addQueryParameter(e.getKey(), v);
            }
        });

        Request okRequest = new Request.Builder()
                .method(request.getVerb().verb(), body)
                .url(urlBuilder.build())
                .headers(headersBuilder.build())
                .build();
        Response response = client.newCall(okRequest).execute();
        return new OkHttpResponse(response);
    }
}
