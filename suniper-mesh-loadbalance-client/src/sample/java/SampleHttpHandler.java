import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

/**
 * @author Rao Mengnan
 *         on 2018/7/3.
 */
public class SampleHttpHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        System.out.println("\nMessage received -------   -------\n");

        System.out.println(msg.getClass());
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            System.err.println("STATUS: " + response.status());
            System.err.println("VERSION: " + response.protocolVersion());
            System.err.println("HEADERS:\n" + response.headers());
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            System.out.println(content.content().toString(CharsetUtil.UTF_8));

            if (content instanceof LastHttpContent) {
                ctx.close();
            }
        }

        System.err.flush();
    }
}
