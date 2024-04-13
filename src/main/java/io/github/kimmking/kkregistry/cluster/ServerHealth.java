package io.github.kimmking.kkregistry.cluster;

import com.alibaba.fastjson.JSON;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/14 01:09
 */

@Slf4j
public class ServerHealth {

    Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void checkServerHealth() {
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate( () -> {
                    //boolean isNeedElect = checkIsNeedElect();
                    checkIsNeedElect();
                    //System.out.println(" ===*****%%%$$$>>> checkIsNeedElect = " + isNeedElect);
                    //doElect(isNeedElect);
                    doElect();
                    System.out.println(" ===*****%%%$$$>>> isLeader=" + cluster.isLeader()
                            + ",myself-version=" + cluster.getMYSELF().getVersion()
                            + ",leader-version=" + cluster.getLeader().getVersion());
                    if(!cluster.isLeader() && cluster.getMYSELF().getVersion() < cluster.getLeader().getVersion()) { // 改成首次刷 TODO
                        // 改成 判断版本号 TODO
                        // 改成 判断LEADER是否改变 TODO
                        // 把这个类拆分为多个类 TODO
                        // 控制读写分离 TODO
                        // 优化实时性同步 TODO
                        System.out.println(" ===*****%%%$$$>>> syncFromLeader: " + cluster.getLeader());
                        long v = syncFromLeader();
                        //System.out.println(" ===*****%%%$$$>>> sync from leader version: " + v);
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void checkIsNeedElect() {
        long start = System.currentTimeMillis();
        //final boolean[] isNeedElects = new boolean[1];
        //if(checkServerInfo(server)) isNeedElects[0] = true;
        cluster.getServers().stream()
                .filter(s-> !s.equals(cluster.MYSELF))
                .parallel().forEach(this::checkServerInfo);
        System.out.println(" =====>>>>>> checkIsNeedElect: " + (System.currentTimeMillis() - start) + " ms");
        // return isNeedElects[0];
    }

    private void doElect(){ //boolean isNeedElect) {
        List<Server> servers = cluster.getServers();
        List<Server> masters = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());
        //if(isNeedElect) {
        //    System.out.println(" =====>>>>>> isNeedElect : true and to check elect ..." + servers);
            if (masters.isEmpty()) {
                log.error(" =========>>>>> ELECT: no masters: {}", servers);
                elect();
            } else if (masters.size() > 1) {
                log.error(" =========>>>>> ELECT: more than one master: {}", masters);
                elect();
            }
//        } else {
//            if (masters.size() > 1) {
//                log.error(" =========>>>>> !isNeedElect more than one master: {}", masters);
//                elect();
//            } else {
//                System.out.println(" =====>>>>>> isNeedElect : false and do not elect ..." + servers);
//            }
//        }
    }

    private void elect() {
        List<Server> servers = cluster.getServers();
        Server candidate = null;
        System.out.println(" ======>>>> ELECT from servers = " + servers);
        if(servers.isEmpty()) {
            candidate = cluster.myself();
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
        if(candidate == null) candidate = cluster.myself();
        System.out.println(" ======>>>> ELECT candidate = " + candidate);
        servers.forEach(server -> server.setLeader(false));
        candidate.setLeader(true);
//        LEADER = candidate;
        System.out.println(" ======>>>> servers after ELECT = " + servers);
    }

    public void checkServerInfo(Server server) {
        //boolean isNeedElect = false;
        try {
            String respJson = cluster.getHttpInvoker().post("", server.getUrl()+"/info");
            Server serverInfo = JSON.parseObject(respJson, Server.class);
            log.info(" =========>>>>> health check success for {}.", server);
            if (!server.isStatus()) {
                server.setStatus(true);
            }
            server.setVersion(serverInfo.getVersion());
            server.setLeader(serverInfo.isLeader());
                //isNeedElect = true;
                //log.info(" ====  =====>>>>> isNeed1 ******** {}.", server);
        } catch (RuntimeException ex) {
            log.error(" =========>>>>> health check failed for {}", server);//, ex);
            if(server.isStatus()) {
                server.setStatus(false);
                //server.setVersion(-1);
                server.setLeader(false);
                //isNeedElect = true;
                //log.info(" =====  ====>>>>> isNeed2 ******** {}.", server);
            }
        }
        //return isNeedElect;
    }

    private long syncFromLeader() {
        try {
            System.out.println(cluster.getLeader().getUrl() + "/snapshot");
            String respJson = cluster.getHttpInvoker().post("", cluster.getLeader().getUrl() + "/snapshot");
            System.out.println(" =====>>>>>> respJson: " + respJson);
            Snapshot snapshot = JSON.parseObject(respJson, Snapshot.class);
            return KKRegistryService.restore(snapshot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

}
