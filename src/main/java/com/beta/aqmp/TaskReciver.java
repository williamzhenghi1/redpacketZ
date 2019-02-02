package com.beta.aqmp;

import com.alibaba.fastjson.JSON;
import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import com.beta.service.RedPacketDAOservice;
import com.beta.service.UserTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskReciver {

    @Autowired
    RedPacketDAOservice redPacketDAOservice;


    @Autowired
    UserTransactionService userTransactionService;

    @RabbitListener(queues = MQConfig.QUEUE_UserBlance)
    void userBalanceTaskMessageReciver(String message)
    {
        UserBalanceTaskMessage userBalanceTaskMessage = JSON.toJavaObject(JSON.parseObject(message),UserBalanceTaskMessage.class);
        //todo 写进数据库,用户数据写进去的时候要额外的做事务控制
        userTransactionService.writeIntoDataBase(userBalanceTaskMessage);
    }

    @RabbitListener(queues = MQConfig.QUEUE_BigRedPacket)
    void BigredPacketLogTaskMessageReciver(String message)
    {
        BigredPacket bigredPacket = JSON.toJavaObject(JSON.parseObject(message),BigredPacket.class);
        //todo 写进数据库
        redPacketDAOservice.save(bigredPacket);
    }

    @RabbitListener(queues = MQConfig.QUEUE_SmallRedPacket)
    void SmallRedPacketLogTaskMessage(String message)
    {
        SmallRedPacket smallRedPacket = JSON.toJavaObject(JSON.parseObject(message),SmallRedPacket.class);
        //todo 写进数据库
        log.info("收到NO. "+smallRedPacket.getRedid()+"的红包");
        redPacketDAOservice.save(smallRedPacket);
    }
}
