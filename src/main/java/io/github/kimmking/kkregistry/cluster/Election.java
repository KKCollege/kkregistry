package io.github.kimmking.kkregistry.cluster;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/14 12:36
 */
public class Election {

    public void elect(Server myself, List<Server> servers) {
        Server candidate = null;
        System.out.println(" ======>>>> ELECT from servers = " + servers);
        if(servers.isEmpty()) {
            candidate = myself;
        } else if(servers.size()==1) {
            candidate = servers.get(0); // myself
        } else {
            for (Server server : servers) {
                if(server.isStatus()) {
                    if (candidate == null) {
                        candidate = server;
                        continue;
                    }
                    if (server.hashCode() < candidate.hashCode()) { // TODO 可以改成比数据版本
                        candidate = server;
                    }
                }
            }
        }
        if(candidate == null) candidate = myself;
        System.out.println(" ======>>>> ELECT candidate = " + candidate);
        servers.forEach(server -> server.setLeader(false));
        candidate.setLeader(true);
        System.out.println(" ======>>>> servers after ELECT = " + servers);
    }

}
