package io.github.kimmking.kkregistry.health;

import io.github.kimmking.kkregistry.health.http.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * OKHttp invoker.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/3/20 20:40
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public String post(String requestString, String url) {
//        String reqJson = JSON.toJSONString(requestString);
        log.debug(" ===> requestString = " + requestString);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
//            RpcResponse<Object> rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return respJson;
        } catch (Exception e) {
            // e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
