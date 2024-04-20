package io.github.kimmking.kkregistry.service;

import io.github.kimmking.kkregistry.cluster.Snapshot;
import io.github.kimmking.kkregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 19:26
 */
public interface RegistryService {

    // 最基础的3个方法
    InstanceMeta register(String service, InstanceMeta instance);
    InstanceMeta unregister(String service, InstanceMeta instance);
    List<InstanceMeta> getAllInstances(String service);

    // todo 添加一些高级功能
    long renew(InstanceMeta instance,String... service);
    Long version(String service);
    Map<String, Long> versions(String... services);

}
