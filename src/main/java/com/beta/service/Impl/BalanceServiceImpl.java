package com.beta.service.Impl;

import com.beta.pojo.User;
import com.beta.redis.RedisService;
import com.beta.redis.UserKey;
import com.beta.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    RedisService redisService;

    @Override
    public double getUserBalance(String id) {
        return redisService.get(UserKey.getById,id,long.class);
    }

    @Override
    public double balanceChange(String id, double balance) {
        return redisService.valueChange(UserKey.getById,id,balance);
    }


}
