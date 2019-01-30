package com.beta.service;

import com.beta.pojo.User;
import com.beta.redis.KeyPrefix;
import com.beta.redis.OrderKey;
import com.beta.redis.RedisService;
import com.beta.redis.UserKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {
    @Autowired
    RedisService redisService;

    public void test1()
    {
        KeyPrefix keyPrefix =new OrderKey("order");
        User user =new User();
        user.setBalance(100);
        user.setUserid(123);
        redisService.set(keyPrefix,"id",user);
    }
    public User test2()
    {
        KeyPrefix keyPrefix =new OrderKey("order");
        User user =new User();
       user= redisService.get(keyPrefix,"id",User.class);
       return user;
    }

    public User AuthUserToken(String id){

        User user = redisService.get(UserKey.token,id,User.class);

        if(user==null)
           throw new RuntimeException("User not Found");
        return user;
    }

}
