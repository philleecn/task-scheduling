package com.phillee.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: Redisson配置类
 * @Author: PhilLee
 * @Date: 2023/8/24 10:29
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.cluster.nodes}")
    private List<String> clusterNodes;

    @Value("${spring.redis.password}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient getRedissonClient() {
        List<String> standardClusterNodes = clusterNodes.stream()
                .map(o -> o = "redis://" + o)
                .collect(Collectors.toList());

        log.info("redis cluster: {}", standardClusterNodes);

        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE);

        ClusterServersConfig clusterServersConfig = config.useClusterServers().addNodeAddress(standardClusterNodes.toArray(new String[0]));
        clusterServersConfig.setPassword(password);
        clusterServersConfig.setPingConnectionInterval(32000);

        return Redisson.create(config);
    }
}
