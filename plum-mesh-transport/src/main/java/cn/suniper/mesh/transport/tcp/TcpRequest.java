package cn.suniper.mesh.transport.tcp;

import com.netflix.client.ClientRequest;

import java.net.URI;
import java.util.UUID;

/**
 * @author Rao Mengnan
 *         on 2018/7/2.
 */
public class TcpRequest extends ClientRequest {
    private int id;
    private Object data;

    public TcpRequest() {
        id = UUID.randomUUID().variant();
    }

    public TcpRequest(ClientRequest request) {
        super(request);
        id = UUID.randomUUID().variant();
    }

    public static class Builder {
        private ClientRequest request;
        private Object data;

        public Builder() {
            this.request = new ClientRequest();
        }

        public Builder(URI uri) {
            this.request = new ClientRequest(uri);
        }

        public Builder(URI uri, Object loadBalancerKey, boolean isRetriable) {
            this.request = new ClientRequest(uri, loadBalancerKey, isRetriable);
        }

        public Builder(ClientRequest request) {
            this.request = new ClientRequest(request);
        }

        public Builder setData(Object data) {
            this.data = data;
            return this;
        }

        public TcpRequest build() {
            TcpRequest tcpRequest = new TcpRequest(request);
            tcpRequest.data = data;
            return tcpRequest;
        }
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public int getId() {
        return id;
    }

}
