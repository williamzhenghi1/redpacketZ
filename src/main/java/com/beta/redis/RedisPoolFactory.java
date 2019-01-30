package com.beta.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {

    @Autowired
    RedisConfig  redisConfig;

    /**
     * 将redis连接池注入spring容器
     * @return
     */
    @Bean
    public JedisPool JedisPoolFactory(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(500);
        config.setMaxTotal(1000);
        config.setMaxWaitMillis(500);
        JedisPool jp = new JedisPool(config, "35.220.159.226", 6379,
                redisConfig.getTimeout()*1000, null, 0);
        return jp;
    }

}
