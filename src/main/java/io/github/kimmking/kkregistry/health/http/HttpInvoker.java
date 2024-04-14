package io.github.kimmking.kkregistry.health.http;

import com.alibaba.fastjson.JSON;
import io.github.kimmking.kkregistry.health.OkHttpInvoker;
import lombok.SneakyThrows;

/**
 * Interface for http invoke.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/3/20 20:39
 */
public interface HttpInvoker {

    HttpInvoker Default = new OkHttpInvoker(300);

    String post(String rpcRequest, String url);

    String get(String url);

    @SneakyThrows
    static <T> T httpGet(String url,  Class<T> clazz) {
        System.out.println(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        System.out.println(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    @SneakyThrows
    static <T> T httpPost(String rpcRequest, String url, Class<T> clazz) {
        System.out.println(" =====>>>>>> httpPost: " + url);
        String respJson = Default.post(rpcRequest, url);
        System.out.println(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }


}
