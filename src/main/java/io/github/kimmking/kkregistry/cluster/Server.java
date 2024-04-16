package io.github.kimmking.kkregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Registry server instance.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/16 20:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {
    private String url;
    private boolean status;
    private boolean leader;
    private long version;
}
