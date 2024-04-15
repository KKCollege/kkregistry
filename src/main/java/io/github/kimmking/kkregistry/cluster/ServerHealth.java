package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.health.HttpInvoker;
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
                    try {
                        updateServer();
                        doElect();

                        log.debug(" ===*****%%%$$$>>> isLeader=" + cluster.isLeader()
                                + ",myself-version=" + cluster.getMYSELF().getVersion()
                                + ",leader-version=" + cluster.getLeader().getVersion());
                        if (!cluster.isLeader() && cluster.getMYSELF().getVersion() < cluster.getLeader().getVersion()) {
                            // 改成首次刷 TODO 优先级低
                            // 改成 判断版本号 DONE
                            // 改成 判断LEADER是否改变 DONE
                            // 把这个类拆分为多个类 DONE
                            // 控制读写分离 TODO 客户端
                            // 优化实时性同步 TODO 优先级低
                            log.debug(" ===*****%%%$$$>>> syncFromLeader: " + cluster.getLeader());
                            long v = syncSnapshotFromLeader();
                            log.debug(" ===*****%%%$$$>>> sync success new version: " + v);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void updateServer() {
        long start = System.currentTimeMillis();
        cluster.getServers().stream()
                .filter(s-> !s.equals(cluster.MYSELF))
                .forEach(this::checkServerInfo);
        log.debug(" =====>>>>>> updateServer info: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void doElect(){
        Election election = new Election();
        List<Server> servers = cluster.getServers();
        List<Server> masters = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());
            if (masters.isEmpty()) {
                log.warn(" =========>>>>> ELECT: no masters: {}", servers);
                election.elect(cluster.myself(), servers);
            } else if (masters.size() > 1) {
                log.warn(" =========>>>>> ELECT: more than one master: {}", masters);
                election.elect(cluster.myself(), servers);
            }
    }

    public void checkServerInfo(Server server) {
        try {
            Server serverInfo = HttpInvoker.httpGet(server.getUrl()+"/info", Server.class);
            log.info(" =========>>>>> health check success for {}.", server);
            if (!server.isStatus()) {
                server.setStatus(true);
            }
            server.setVersion(serverInfo.getVersion());
            server.setLeader(serverInfo.isLeader());
        } catch (RuntimeException ex) {
            log.warn(" =========>>>>> health check failed for {}", server);//, ex);
            if(server.isStatus()) {
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    private long syncSnapshotFromLeader() {
        try {
            log.info(" =========>>>>> syncSnapshotFromLeader {}", cluster.getLeader().getUrl() + "/snapshot");
            Snapshot snapshot = HttpInvoker.httpGet(cluster.getLeader().getUrl() + "/snapshot", Snapshot.class);
            return KKRegistryService.restore(snapshot);
        } catch (Exception ex) {
            log.error(" =========>>>>> syncSnapshotFromLeader failed.", ex);
        }
        return -1;
    }

}
