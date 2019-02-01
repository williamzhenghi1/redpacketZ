package com.beta.aqmp;

import com.alibaba.fastjson.JSON;
import com.beta.RedpacketApplication;
import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskSender {
    @Autowired
    AmqpTemplate amqpTemplate;

   public void sendUserBalanceInfoService(UserBalanceTaskMessage userBalanceTaskMessage)
    {
        amqpTemplate.convertAndSend(MQConfig.QUEUE_UserBlance, JSON.toJSONString(userBalanceTaskMessage));
    }

    public void SendBigRedPacket(BigredPacket bigredPacket)
    {
        amqpTemplate.convertAndSend(MQConfig.QUEUE_BigRedPacket,JSON.toJSONString(bigredPacket));
    }

    public void SendSmallRedPacket(SmallRedPacket smallRedPacket)
    {
        amqpTemplate.convertAndSend(MQConfig.QUEUE_SmallRedPacket,JSON.toJSONString(smallRedPacket));
    }
}
