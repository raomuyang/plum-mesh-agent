package cn.suniper.mesh.transport.http;

import okhttp3.*;
import okio.BufferedSource;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class OkHttpResponseTest {
    @Test
    public void testCreateSuccessResponse() {
        Response response = new Response.Builder()
                .addHeader("a", "1")
                .addHeader("b", "2")
                .code(200)
                .body(new ResponseBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .message("test")
                .request(new Request.Builder().url("http://suniper.cn").build())
                .protocol(Protocol.HTTP_1_1)
                .build();
        OkHttpResponse okHttpResponse = new OkHttpResponse(response);
        assertTrue(okHttpResponse.isSuccess());
        assertTrue(okHttpResponse.hasPayload());
        assertEquals(2, okHttpResponse.getHeaders().size());
    }

    @Test
    public void testCreateFaileResponse() {
        Response response = new Response.Builder()
                .addHeader("a", "1")
                .addHeader("b", "2")
                .addHeader("c", "3")
                .code(400)
                .body(new ResponseBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .message("test")
                .request(new Request.Builder().url("http://suniper.cn").build())
                .protocol(Protocol.HTTP_1_1)
                .build();
        OkHttpResponse okHttpResponse = new OkHttpResponse(response);
        assertFalse(okHttpResponse.isSuccess());
        assertTrue(okHttpResponse.hasPayload());
        assertEquals(3, okHttpResponse.getHeaders().size());
    }
}