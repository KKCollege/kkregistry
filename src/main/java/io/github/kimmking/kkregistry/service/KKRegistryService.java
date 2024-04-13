package io.github.kimmking.kkregistry.service;

import io.github.kimmking.kkregistry.cluster.Snapshot;
import io.github.kimmking.kkregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Default implementation of RegistryService.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 19:33
 */

@Slf4j
public class KKRegistryService implements RegistryService {

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    final static AtomicLong VERSION = new AtomicLong(0);


    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas != null && !metas.isEmpty()) {
            if(metas.contains(instance)) {
                log.info(" ====> instance {} already registered", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        log.info(" ====> register instance {}", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas == null || metas.isEmpty()) {
            return null;
        }
        log.info(" ====> unregister instance {}", instance.toUrl());
        metas.removeIf( m -> m.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    public synchronized long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service+"@"+instance.toUrl(), now);
        }
        return now;
    }

    public Long version(String service) {
        return VERSIONS.get(service);
    }

    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services)
                .collect(Collectors.toMap(x->x, VERSIONS::get, (a, b)->b));
    }

    public synchronized Snapshot snapshot() {
        MultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new ConcurrentHashMap<>(VERSIONS);
        Map<String, Long> timestamps = new ConcurrentHashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }
}
