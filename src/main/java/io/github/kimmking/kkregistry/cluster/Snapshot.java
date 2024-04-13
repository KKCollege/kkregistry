package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.model.InstanceMeta;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * Snapshot for registry.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 21:25
 */

@Data
public class Snapshot {

    final LinkedMultiValueMap<String, InstanceMeta> REGISTRY;
    final Map<String, Long> VERSIONS;
    final Map<String, Long> TIMESTAMPS;
    final long version;

    public Snapshot(LinkedMultiValueMap<String, InstanceMeta> registry,
                    Map<String, Long> versions,
                    Map<String, Long> timestamps,
                    long version)
    {
        this.VERSIONS = versions;
        this.TIMESTAMPS = timestamps;
        this.REGISTRY = registry;
        this.version = version;
    }

}
