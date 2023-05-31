package com.yue.spring.config;

import com.yue.spring.handler.utils.RedissonLockUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://r-bp176pjvb6cx872hb0pd.redis.rds.aliyuncs.com:6379")
                .setPassword("Liu021116");
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }

    @Bean
    public RedissonLockUtil redissonLockUtil(RedissonClient redissonClient){
        RedissonLockUtil redissonLockUtil=new RedissonLockUtil();
        redissonLockUtil.setRedissonClient(redissonClient);
        return redissonLockUtil;
    }
}
