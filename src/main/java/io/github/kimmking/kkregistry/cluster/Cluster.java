package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.RegistryConfigProperties;
import io.github.kimmking.kkregistry.health.OkHttpInvoker;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 21:41
 */

@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    static String ip;
    Server MYSELF;

    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "127.0.0.1";
        }
    }



    @SneakyThrows
    public Server myself() { //192.168.110.220
        if(MYSELF == null) {
            Server my = new Server("http://" + ip + ":" + port, false, true);
            System.out.println(" ========>>>>>>  myself: " + my);
            MYSELF = my;
        }
        return MYSELF;
    }

    RegistryConfigProperties registryConfigProperties;
    @Getter
    List<Server> servers;

    public Cluster(RegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    private void checkHealth() {
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate( () -> {
                    boolean isNeedElect = false;
                    for (Server server : servers) {
                        if(health(server)) isNeedElect = true;
                    }
                    List<Server> masters = servers.stream().filter(Server::isStatus)
                            .filter(Server::isLeader).collect(Collectors.toList());
                    if(isNeedElect) {
                        System.out.println(" =====>>>>>> isNeedElect : true and to check elect ...");
                        if (masters.isEmpty()) {
                            log.error(" =========>>>>> no master: {}", servers);
                            elect();
                        } else if (masters.size() > 1) {
                            log.error(" =========>>>>> isNeedElect more than one master: {}", masters);
                            elect();
                        }
                    } else {
                        if (masters.size() > 1) {
                            log.error(" =========>>>>> !isNeedElect more than one master: {}", masters);
                            elect();
                        } else {
                            System.out.println(" =====>>>>>> isNeedElect : false and do not elect ...");
                        }
                    }
                }
                , 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void elect() {
        Server candidate = null;
        System.out.println(" ======>>>> ELECT servers = " + servers);
        if(servers.isEmpty()) {
            candidate = myself();
        } else if(servers.size()==1) {
            candidate = servers.get(0); // myself
        } else {
            for (Server server : servers) {
                if(server.isStatus()) {
                    if (candidate == null) {
                        candidate = server;
                        continue;
                    }
                    if (server.hashCode() < candidate.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if(candidate == null) candidate = myself();
        System.out.println(" ======>>>> candidate = " + candidate);
        servers.forEach(server -> server.setLeader(false));
        candidate.setLeader(true);
        System.out.println(" ======>>>> servers /elect = " + servers);
    }

    OkHttpInvoker httpInvoker = new OkHttpInvoker(1000);

    private boolean health(Server server) {
        boolean isNeedElect = false;
        try {
            String respJson = httpInvoker.post("", server.getUrl()+"/info");
            if("M".equals(respJson) || "S".equals(respJson)) {
                log.info(" =========>>>>> health check success for {}.", server);
                if(!server.isStatus()) {
                    server.setStatus(true);
                    isNeedElect = true;
                    log.info(" ====  =====>>>>> isNeed1 ******** {}.", server);
                }
                server.setLeader(respJson.equals("M"));
            } else {
                log.warn(" =========>>>>> health check unknown info: {} for {}.", respJson, server);
            }
        } catch (RuntimeException ex) {
            log.error(" =========>>>>> health check failed for " + server); //, ex);
            if(server.isStatus()) {
                server.setStatus(false);
                isNeedElect = true;
                log.info(" =====  ====>>>>> isNeed2 ******** {}.", server);
            }
        }
        return isNeedElect;
    }

    public void init() {
        myself();
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerlist()){
            if(MYSELF.getUrl().equals(url)) {
                servers.add(MYSELF);
            } else {
                Server server = new Server();
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                servers.add(server);
            }
        }
        this.servers = servers;
        checkHealth();
    }

    public Server getLeader() {
        return this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).findFirst().orElse(null);
    }

    public boolean isLeader() {
        if(MYSELF==null) {
            myself();
        }
        return MYSELF.isLeader();
    }

}
