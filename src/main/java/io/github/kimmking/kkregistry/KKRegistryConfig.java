package io.github.kimmking.kkregistry;

import io.github.kimmking.kkregistry.cluster.Cluster;
import io.github.kimmking.kkregistry.health.HealthChecker;
import io.github.kimmking.kkregistry.health.KKHealthChecker;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import io.github.kimmking.kkregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configuration for all beans.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 19:50
 */

@Configuration
//@Import({KKRegistryConfig.class})
public class KKRegistryConfig {

    @Bean
    public RegistryService registryService()
    {
        return new KKRegistryService();
    }

//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
//        return new KKHealthChecker(registryService);
//    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired RegistryConfigProperties registryConfigProperties) {
        return new Cluster(registryConfigProperties);
    }

}
