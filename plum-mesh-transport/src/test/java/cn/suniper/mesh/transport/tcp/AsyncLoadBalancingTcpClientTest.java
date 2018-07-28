package cn.suniper.mesh.transport.tcp;

import com.netflix.client.RequestSpecificRetryHandler;
import org.junit.Test;

import java.net.SocketException;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class AsyncLoadBalancingTcpClientTest {
    @Test
    public void getRequestSpecificRetryHandler() throws Exception {
        TcpRequest request = new TcpRequest.Builder(new URI("http://127.0.0.1"), null, true).build();

        AsyncLoadBalancingTcpClient client = new AsyncLoadBalancingTcpClient();
        RequestSpecificRetryHandler handler = client.getRequestSpecificRetryHandler(request, null);
        assertTrue(handler.isRetriableException(new SocketException(), false));

        request = new TcpRequest.Builder(new URI("http://127.0.0.1"), null, false).build();
        handler = client.getRequestSpecificRetryHandler(request, null);
        assertFalse(handler.isRetriableException(new SocketException(), false));
    }

}