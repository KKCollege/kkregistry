package io.github.kimmking.kkregistry.health;

import io.github.kimmking.kkregistry.model.InstanceMeta;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import io.github.kimmking.kkregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of HealthChecker.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 20:41
 */

@Slf4j
public class KKHealthChecker implements HealthChecker {

    RegistryService registryService;

    public KKHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    long timeout = 20_000;

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(
                () -> {
                    log.info(" ===> Health checker running...");
                    long now = System.currentTimeMillis();
                    KKRegistryService.TIMESTAMPS.keySet().stream().forEach(serviceAndInst -> {
                        long timestamp = KKRegistryService.TIMESTAMPS.get(serviceAndInst);
                        if (now - timestamp > timeout) {
                            log.info(" ===> Health checker: {} is down", serviceAndInst);
                            int index = serviceAndInst.indexOf("@");
                            String service = serviceAndInst.substring(0, index);
                            String url = serviceAndInst.substring(index + 1);
                            InstanceMeta instance = InstanceMeta.from(url);
                            registryService.unregister(service, instance);
                            KKRegistryService.TIMESTAMPS.remove(serviceAndInst);
                        }
                    });

                },
                10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
