package com.shrinkr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${REDIS_URL:redis://localhost:6379}")
    private String redisUrl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() throws Exception {
        URI uri = new URI(redisUrl);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 6379 : uri.getPort();
        String userInfo = uri.getUserInfo();

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

        if (userInfo != null && userInfo.contains(":")) {
            String password = userInfo.split(":", 2)[1];
            config.setPassword(password);
        }

        LettuceClientConfiguration clientConfig;
        if (redisUrl.startsWith("rediss://")) {
            clientConfig = LettuceClientConfiguration.builder()
                    .useSsl()
                    .build();
        } else {
            clientConfig = LettuceClientConfiguration.builder().build();
        }

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}