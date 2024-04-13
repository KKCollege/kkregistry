package io.github.kimmking.kkregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 21:47
 */

@ConfigurationProperties(prefix = "registry")
@Data
public class RegistryConfigProperties {
    List<String> serverlist;
}
