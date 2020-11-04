package io.github.kimmking.gateway.outbound.httpclient;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpClientOutboundHandler {

    private final CloseableHttpClient httpclient;
    private final String backendUrl;

    private static final String HOST = "localhost";
    private static final int PORT = 8088;

    public HttpClientOutboundHandler(String backendUrl) {
        this.httpclient = HttpClientPoolUtil.getInstance(HOST, PORT).getHttpClient();
        this.backendUrl = backendUrl;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        FullHttpResponse fullHttpResponse = null;
        try {
            fullHttpResponse = fetchGet(fullRequest);
        } catch (Exception e) {
            fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            ctx.close();
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(fullHttpResponse);
                }
            }
            ctx.flush();
        }
    }

    private FullHttpResponse fetchGet(final FullHttpRequest fullRequest) throws IOException {
        CloseableHttpResponse endpointResponse = getHttpResponse(fullRequest);
        return getFullHttpResponse(endpointResponse);
    }

    private CloseableHttpResponse getHttpResponse(final FullHttpRequest fullRequest) throws IOException {
        final String url = this.backendUrl + fullRequest.uri();
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        return httpclient.execute(httpGet);
    }

    private FullHttpResponse getFullHttpResponse(CloseableHttpResponse endpointResponse) throws IOException {
        byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.getFirstHeader("Content-Length").getValue()));
        return response;
    }
}
