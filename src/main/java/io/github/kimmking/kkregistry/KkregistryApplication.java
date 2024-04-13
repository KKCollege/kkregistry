package io.github.kimmking.kkregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RegistryConfigProperties.class})
public class KkregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkregistryApplication.class, args);
    }

}
