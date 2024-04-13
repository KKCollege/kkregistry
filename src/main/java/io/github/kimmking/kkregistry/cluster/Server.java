package io.github.kimmking.kkregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 21:43
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    private String url;
    private boolean leader;
    private boolean status;
    private long version;
}
