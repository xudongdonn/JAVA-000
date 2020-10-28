package com.it.test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Objects;

public class HttpclientTest {

    public static String doGet(String url) {
        return execute(url);
    }

    private static CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private static HttpGet getGet(String url) {
        return new HttpGet(url);
    }

    public static String execute(String url) {
        CloseableHttpResponse response = null;
        String result = null;
        try {
            response = getHttpClient().execute(getGet(url));
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(response)) {
                    response.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
        return result;
    }

}
