package io.github.kimmking.kkregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * registry config properties.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/16 20:24
 */

@Data
@ConfigurationProperties(prefix = "kkregistry")
public class KKRegistryConfigProperties {

    private List<String> serverList;

}
