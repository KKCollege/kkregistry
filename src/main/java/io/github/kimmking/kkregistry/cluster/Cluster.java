package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.KKRegistryConfigProperties;
import io.github.kimmking.kkregistry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Registry cluster.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/16 20:19
 */

@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    String host;

    Server MYSELF;

    KKRegistryConfigProperties registryConfigProperties;

    public Cluster(KKRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    @Getter
    private List<Server> servers;
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    long timeout = 5_000;

    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            System.out.println(" ===> findFirstNonLoopbackHostInfo = " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        System.out.println(" ===> myself = " + MYSELF);

        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            if(url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if(url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if(url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        } // todo ...
        this.servers = servers;

        executor.scheduleAtFixedRate( () -> {
                    try {
                        updateServers();
                        electLeader();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                , 0, timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void electLeader() {
        List<Server> masters = this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).toList();
        if(masters.isEmpty()) {
            System.out.println(" ===>>> ^&**^&&** elect for no leader: " + servers);
            elect();
        } else if(masters.size() > 1) {
            System.out.println("  ^&**^&&** ===>>> ^&**^&&** elect for more than one leader: " + servers);
            elect();
        } else {
            System.out.println(" ===>>> no need election for leader: " + masters.get(0));
        }
    }

    private void elect() {
        // 1.各种节点自己选，算法保证大家选的是同一个
        // 2.外部有一个分布式锁，谁拿到锁，谁是主
        // 3.分布式一致性算法，比如paxos,raft，，很复杂
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if(server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            System.out.println(" ===>>> elect for leader: " + candidate);
        } else {
            System.out.println(" ===>>> elect failed for no leaders: " + servers);
        }
    }

    private void updateServers() {
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                System.out.println(" ===>>> health check success for " + serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception ex) {
                System.out.println(" ===>>> health check failed for " + server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }


}
