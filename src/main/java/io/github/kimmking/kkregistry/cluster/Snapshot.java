package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/20 下午7:35
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Snapshot {
    LinkedMultiValueMap<String, InstanceMeta> REGISTRY;
    Map<String, Long> VERSIONS;
    Map<String, Long> TIMESTAMPS;
    long version;
}
