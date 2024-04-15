package io.github.kimmking.kkregistry.health;

import com.alibaba.fastjson.JSON;
import io.github.kimmking.kkregistry.health.http.OkHttpInvoker;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for http invoke.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/3/20 20:39
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String rpcRequest, String url);

    String get(String url);

    @SneakyThrows
    static <T> T httpGet(String url,  Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    @SneakyThrows
    static <T> T httpPost(String rpcRequest, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String respJson = Default.post(rpcRequest, url);
        log.debug(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }


}
