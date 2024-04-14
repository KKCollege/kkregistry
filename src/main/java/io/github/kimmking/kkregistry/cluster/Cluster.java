package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.RegistryConfigProperties;
import io.github.kimmking.kkregistry.health.OkHttpInvoker;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

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

    @Getter
    Server MYSELF;
//    Server LEADER;

    static {
        try {
            ip = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            System.out.println(" ===>>> findFirstNonLoopbackHostInfo().getIpAddress() = " + ip);
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
    }

    @Getter
    OkHttpInvoker httpInvoker = new OkHttpInvoker(1000);

    @SneakyThrows
    public Server myself() { //192.168.110.220
        if(MYSELF == null) {
            Server my = new Server("http://" + ip + ":" + port, false, true, -1);
            System.out.println(" ========>>>>>>  myself: " + my);
            MYSELF = my;
        }
        MYSELF.setVersion(KKRegistryService.VERSION.get());
        return MYSELF;
    }

    RegistryConfigProperties registryConfigProperties;
    @Getter
    List<Server> servers;

    ServerHealth serverHealth;

    public Cluster(RegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        myself();
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerlist()){
            if(MYSELF.getUrl().equals(url)) {
                servers.add(MYSELF);
            } else {
                Server server = new Server();
                server.setUrl(convertLocalhost(url));
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1);
                servers.add(server);
            }
        }
        this.servers = servers;
        serverHealth = new ServerHealth(this);
        serverHealth.checkServerHealth();
    }

    private String convertLocalhost(String url) {
        if(url.contains("localhost")) {
            return url.replace("localhost", ip);
        } if(url.contains("127.0.0.1")) {
            return url.replace("127.0.0.1", ip);
        } else {
            return url;
        }
    }

    public Server getLeader() {
        return this.servers.stream().filter(Server::isStatus)
                    .filter(Server::isLeader).findFirst().orElse(null);
    }

    public boolean isLeader() {
        return myself().isLeader();
    }
}
