package cn.suniper.mesh.transport.http;

import com.netflix.client.RequestSpecificRetryHandler;
import com.netflix.client.http.HttpRequest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class LoadBalancingHttpClientTest {
    @Test
    public void getRequestSpecificRetryHandler() throws Exception {
        LoadBalancingHttpClient client = new LoadBalancingHttpClient();
        HttpRequest request = HttpRequest.newBuilder().verb(HttpRequest.Verb.GET).build();
        RequestSpecificRetryHandler handler = client.getRequestSpecificRetryHandler(request, null);
        assertTrue(handler.isRetriableException(new Exception(), true));

        request = HttpRequest.newBuilder().verb(HttpRequest.Verb.POST).build();
        handler = client.getRequestSpecificRetryHandler(request, null);
        assertFalse(handler.isRetriableException(new Exception(), true));
    }

}