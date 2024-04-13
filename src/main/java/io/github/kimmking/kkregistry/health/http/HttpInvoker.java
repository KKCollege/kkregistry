package io.github.kimmking.kkregistry.health.http;

/**
 * Interface for http invoke.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/3/20 20:39
 */
public interface HttpInvoker {

    String post(String rpcRequest, String url);

}
