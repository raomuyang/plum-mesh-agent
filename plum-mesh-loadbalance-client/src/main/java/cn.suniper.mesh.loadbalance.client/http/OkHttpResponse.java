package cn.suniper.mesh.loadbalance.client.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.netflix.client.ClientException;
import com.netflix.client.http.CaseInsensitiveMultiMap;
import com.netflix.client.http.HttpHeaders;
import com.netflix.client.http.HttpResponse;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rao Mengnan
 *         on 2018/6/21.
 */
class OkHttpResponse implements HttpResponse {

    private Response response;
    private ResponseBody body;

    public OkHttpResponse(Response response) {
        this.response = response;
        this.body = response.body();
    }

    @Override
    public int getStatus() {
        return response.code();
    }

    @Override
    public String getStatusLine() {

        return response.message();
    }

    @Override
    public Object getPayload() throws ClientException {
        if (hasPayload()) {
            return body.byteStream();
        }
        return null;
    }

    @Override
    public boolean hasPayload() {
        return this.body != null;
    }

    @Override
    public boolean isSuccess() {
        return response.isSuccessful();
    }

    @Override
    public URI getRequestedURI() {
        return response.request().url().uri();
    }

    @Override
    public Map<String, Collection<String>> getHeaders() {
        Multimap<String, String> map = ArrayListMultimap.create();
        response.headers().toMultimap().entrySet().forEach(e -> {
            for (String v : e.getValue()) {
                map.put(e.getKey(), v);
            }
        });
        return map.asMap();
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        final CaseInsensitiveMultiMap headers = new CaseInsensitiveMultiMap();
        response.headers().toMultimap().entrySet().forEach(entry -> {
            for (String value : entry.getValue()) {
                headers.addHeader(entry.getKey(), value);
            }
        });

        return headers;
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public InputStream getInputStream() {
        if (body == null) return null;
        return body.byteStream();
    }

    @Override
    public boolean hasEntity() {
        return hasPayload();
    }

    @Override
    public <T> T getEntity(Class<T> type) throws Exception {
        return null;
    }

    @Override
    public <T> T getEntity(Type type) throws Exception {
        return null;
    }

    @Override
    public <T> T getEntity(TypeToken<T> type) throws Exception {
        return null;
    }
}