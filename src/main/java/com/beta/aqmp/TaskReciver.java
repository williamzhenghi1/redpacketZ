package com.beta.aqmp;

import com.alibaba.fastjson.JSON;
import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TaskReciver {


    @RabbitListener(queues = MQConfig.QUEUE_UserBlance)
    void userBalanceTaskMessageReciver(String message)
    {
        UserBalanceTaskMessage userBalanceTaskMessage = JSON.toJavaObject(JSON.parseObject(message),UserBalanceTaskMessage.class);
        //todo 写进数据库
    }

    @RabbitListener(queues = MQConfig.QUEUE_BigRedPacket)
    void BigredPacketLogTaskMessageReciver(String message)
    {
        BigredPacket BigredPacket = JSON.toJavaObject(JSON.parseObject(message),BigredPacket.class);
        //todo 写进数据库
    }

    @RabbitListener(queues = MQConfig.QUEUE_SmallRedPacket)
    void SmallRedPacketLogTaskMessage(String message)
    {
        SmallRedPacket smallRedPacket = JSON.toJavaObject(JSON.parseObject(message),SmallRedPacket.class);
        //todo 写进数据库
    }
}
