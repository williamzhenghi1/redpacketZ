package com.beta.controller;

import com.beta.Vo.ResultsVo;
import com.beta.pojo.User;
import com.beta.redis.RedisService;
import com.beta.redis.UserKey;
import com.beta.service.BalanceService;
import com.beta.service.TestService;
import org.hibernate.annotations.Synchronize;
import org.hibernate.resource.transaction.backend.jta.internal.synchronization.SynchronizationCallbackCoordinatorNonTrackingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class redpacketController {

    @Autowired
    TestService testService;

    @Autowired
    RedisService redisService;

    @Autowired
    BalanceService balanceService;


    @GetMapping("/test")
    String test()
    {
        testService.test1();

        return testService.test2().toString();
    }

    @GetMapping("/do_makeAReadPacket")
    public ResultsVo  doMakeAreadPacket(@RequestParam("userId")String userId,@RequestParam("price")Double price,@RequestParam("Token")String token,@RequestParam("part")Integer part)
    {
        //todo 用户存在校验

       testService.AuthUserToken(userId);

        Double balance =balanceService.getUserBalance(userId);


        // CAS写入

        do{
            balanceService.balanceChange(userId,price);
            double expection = balance-price;
            if(expection<0)
                throw new RuntimeException("Balance Not Enough");
           double really= balanceService.balanceChange(userId,-price);
           if(really==expection)
               break;
        }while (true);

        //todo 调用红包具体服务 拆分红包写入数据库



        //todo 写入成功后，保证数据库最终一致性，丢进队列



        //todo 调用余额变动服务



        return new ResultsVo();

    }


    @GetMapping("/test2")
    double Test2()
    {

//        redisService.set(UserKey.getById,"1",20.213);



//        return redisService.decrBy(UserKey.getById,"1",-100.00);
    }
}
