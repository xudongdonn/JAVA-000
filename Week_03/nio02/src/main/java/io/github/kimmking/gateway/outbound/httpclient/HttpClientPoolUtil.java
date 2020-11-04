package io.github.kimmking.gateway.outbound.httpclient;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Objects;

public class HttpClientPoolUtil {

    private static final int MAX_CONN = 100;
    private static final int MAX_PRE_ROUTE = 2;
    private static final int MAX_ROUTE = 20;
    private static final int RETRY_COUNT = 3;

    private static HttpClientPoolUtil httpClientPoolUtil;

    private final String host;
    private final int port;

    private HttpClientPoolUtil(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static HttpClientPoolUtil getInstance(String host, int port) {
        if (Objects.isNull(httpClientPoolUtil)) {
            synchronized (HttpClientPoolUtil.class) {
                if (Objects.isNull(httpClientPoolUtil)) {
                    httpClientPoolUtil = new HttpClientPoolUtil(host, port);
                }
            }
        }
        return httpClientPoolUtil;
    }

    public CloseableHttpClient getHttpClient() {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        //设置连接参数
        // 最大连接数
        manager.setMaxTotal(MAX_CONN);
        // 路由最大连接数
        manager.setDefaultMaxPerRoute(MAX_PRE_ROUTE);

        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);
        //请求失败时,进行请求重试
        HttpRequestRetryHandler handler = (e, i, httpContext) -> {
            if (i > RETRY_COUNT) {
                //重试超过3次,放弃请求
                return false;
            }
            HttpClientContext context = HttpClientContext.adapt(httpContext);
            HttpRequest request = context.getRequest();
            //如果请求不是关闭连接的请求
            return !(request instanceof HttpEntityEnclosingRequest);
        };
        return HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
    }
}
