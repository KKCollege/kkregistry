package io.github.kimmking.kkregistry.cluster;

import com.alibaba.fastjson.JSON;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import lombok.SneakyThrows;
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
                    checkIsNeedElect();
                    doElect();
                    System.out.println(" ===*****%%%$$$>>> isLeader=" + cluster.isLeader()
                            + ",myself-version=" + cluster.getMYSELF().getVersion()
                            + ",leader-version=" + cluster.getLeader().getVersion());
                    if(!cluster.isLeader() && cluster.getMYSELF().getVersion() < cluster.getLeader().getVersion()) {
                        // 改成首次刷 TODO
                        // 改成 判断版本号 TODO
                        // 改成 判断LEADER是否改变 TODO
                        // 把这个类拆分为多个类 TODO
                        // 控制读写分离 TODO
                        // 优化实时性同步 TODO
                        System.out.println(" ===*****%%%$$$>>> syncFromLeader: " + cluster.getLeader());
                        long v = syncFromLeader();
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void checkIsNeedElect() {
        long start = System.currentTimeMillis();
        cluster.getServers().stream()
                .filter(s-> !s.equals(cluster.MYSELF))
                .parallel().forEach(this::checkServerInfo);
        System.out.println(" =====>>>>>> checkIsNeedElect: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void doElect(){
        Election election = new Election();
        List<Server> servers = cluster.getServers();
        List<Server> masters = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());
            if (masters.isEmpty()) {
                log.error(" =========>>>>> ELECT: no masters: {}", servers);
                election.elect(cluster.myself(), servers);
            } else if (masters.size() > 1) {
                log.error(" =========>>>>> ELECT: more than one master: {}", masters);
                election.elect(cluster.myself(), servers);
            }
    }

    public void checkServerInfo(Server server) {
        try {
            Server serverInfo = httpGet(server.getUrl()+"/info", Server.class);
            log.info(" =========>>>>> health check success for {}.", server);
            if (!server.isStatus()) {
                server.setStatus(true);
            }
            server.setVersion(serverInfo.getVersion());
            server.setLeader(serverInfo.isLeader());
        } catch (RuntimeException ex) {
            log.error(" =========>>>>> health check failed for {}", server);//, ex);
            if(server.isStatus()) {
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    private long syncFromLeader() {
        try {
            Snapshot snapshot = httpGet(cluster.getLeader().getUrl() + "/snapshot", Snapshot.class);
            return KKRegistryService.restore(snapshot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @SneakyThrows
    public <T> T httpGet(String url,  Class<T> clazz) {
            System.out.println(" =====>>>>>> query: " + url);
            String respJson = cluster.getHttpInvoker().post("", url);
            System.out.println(" =====>>>>>> respJson: " + respJson);
            return JSON.parseObject(respJson, clazz);
    }

}
