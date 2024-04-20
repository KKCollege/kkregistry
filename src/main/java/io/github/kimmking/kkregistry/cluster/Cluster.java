package io.github.kimmking.kkregistry.cluster;

import io.github.kimmking.kkregistry.KKRegistryConfigProperties;
import io.github.kimmking.kkregistry.service.KKRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

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

    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" ===> findFirstNonLoopbackHostInfo = " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info(" ===> myself = " + MYSELF);

        initServers();
        new ServerHealth(this).checkServerHealth();
    }

    private void initServers() {
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
    }


    public Server self() {
        MYSELF.setVersion(KKRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }


}
