package com.beta.controller;

import com.alibaba.fastjson.JSON;
import com.beta.Utils.ResultsVoUtils;
import com.beta.Vo.ResultsVo;

import com.beta.aqmp.TaskSender;
import com.beta.aqmp.UserBalanceTaskMessage;
import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import com.beta.pojo.User;
import com.beta.redis.ListKey;
import com.beta.redis.RedisService;
import com.beta.redis.UserKey;
import com.beta.service.BalanceService;
import com.beta.service.ReadPacketService;
import com.beta.service.TestService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Synchronize;
import org.hibernate.resource.transaction.backend.jta.internal.synchronization.SynchronizationCallbackCoordinatorNonTrackingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@Slf4j
public class redpacketController {

    @Autowired
    TestService testService;

    @Autowired
    RedisService redisService;


    @Autowired
    BalanceService balanceService;

    @Autowired
    ReadPacketService readPacketService;

    @Autowired
    TaskSender taskSender;


    private RateLimiter rateLimiter = RateLimiter.create(500);

    ArrayList<BigredPacket> bigredPackets;

    @PostConstruct
    void postConsturct()
    {
        bigredPackets =new ArrayList<>(20000);
    }


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

//       testService.AuthUserToken(userId);

        redisService.set(UserKey.getById,"1",200.0);

        Double balance =balanceService.getUserBalance(userId);

        if(price==0)
            return ResultsVoUtils.fail();



        /*
             CAS写入
         */

        //todo 这里把cas写入也换成事务
//        do{
//            double expection = balance-price;
//            if(expection<0)
//            {
//                log.info("expection________"+expection);
//                throw new RuntimeException("Balance Not Enough");
//            }
//           double really= balanceService.balanceChange(userId,-price);
//            log.info("really____"+really+"expection___"+expection);
//           if(really==expection)
//               break;
//           else
//           {
//               balanceService.balanceChange(userId,price);
//           }
//        }while (true);

        boolean result1 =false;
        while (true)
        {
            if(balanceService.getUserBalance(userId)-price>=0 )
              result1=  redisService.decrbyFloatTrans(UserKey.getById,userId,price);
            else
                return ResultsVoUtils.fail();
            if(result1)
                break;
        }




        /*
               红包拆分,大红包拆分成小红包
         */

        BigredPacket bigredPacket =new BigredPacket();
        bigredPacket.setRed_id(UUID.randomUUID().toString());
        bigredPacket.setAmmount(price);
        bigredPacket.setUserbelong(userId);
        bigredPacket.setCount(part);
        ArrayList<SmallRedPacket> smallRedPacketArrayList= readPacketService.division(bigredPacket,part);
        bigredPackets.add(bigredPacket);

        /*
            小红包被保存在redis里面，大红包保存在队列里面，
         */

//        redisService.set(ListKey.getListKey,bigredPacket.getRed_id(),smallRedPacketArrayList);

        //这里重写了，用事务保存在队列里面

        for(int i=0;i<smallRedPacketArrayList.size();i++)
        {
            boolean result=true;
           do{
               result= redisService.tset(ListKey.getListKey,bigredPacket.getRed_id(),smallRedPacketArrayList.get(i));
           }while (!result);
            taskSender.SendSmallRedPacket(smallRedPacketArrayList.get(i));
        }
//        ArrayList<SmallRedPacket> smallRedPacketArrayList1 = new ArrayList<>();
//        while (true)
//        {
//            SmallRedPacket smallRedPacket =redisService.tget(ListKey.getListKey,bigredPacket.getRed_id(),SmallRedPacket.class);
//            if(smallRedPacket==null)
//                break;
//            smallRedPacketArrayList1.add(smallRedPacket);
//
//        }

//        return ResultsVoUtils.success("success");



//        这个是读红包
//        return redisService.getArray(ListKey.getListKey,bigredPacket.getRed_id()).toString();


        /*
            写入成功后，保证数据库最终一致性，用户余额操作丢进队列，完成发红包服务
            余额变动服务
         */

        UserBalanceTaskMessage userBalanceTaskMessage = new UserBalanceTaskMessage();
        userBalanceTaskMessage.setBalanceChange(price);
        userBalanceTaskMessage.setUserId(userId);
        taskSender.sendUserBalanceInfoService(userBalanceTaskMessage);
        /*
            异步红包记录
         */
        taskSender.SendBigRedPacket(bigredPacket);

        return  ResultsVoUtils.success("success");
    }

    @GetMapping("/Do_getARandomRedPacket")
    public ResultsVo Do_getARandomRedPacket(@RequestParam("UserId")String userId,
                                            @RequestParam("token")String token)
    {



        //目前设计500QPS
        if(!rateLimiter.tryAcquire())
            return ResultsVoUtils.fail();

          //todo 用户校验

           //随机拿到一个大红包
           int size = bigredPackets.size();

           //所有红包都被抢完了
           if(size==0)
           {
               return ResultsVoUtils.fail();
           }

           //大红包里面拿一个小红包
           int random = new Random().nextInt(size);
           BigredPacket bigredPacket =bigredPackets.get(random);

           log.info("拿到了"+size+"号红包");
           if(bigredPacket.isOver())
             return ResultsVoUtils.fail();

           //已经抢过一次了
            if(bigredPacket.getUserList().contains(userId))
            {
                log.info("重复抢购");
                return ResultsVoUtils.fail();
            }


//           SmallRedPacket smallRedPacket;

//             do{
//                 //大红包被取完了
//                 if(bigredPacket.isOver())
//                     return ResultsVoUtils.fail();
//
//                 //去取这个大红包的小红包
//                 List<SmallRedPacket> smallRedPacketList=  redisService.getArray(ListKey.getListKey,bigredPacket.getRed_id());
//
//                 //已经没了 手慢一步
//                 if(smallRedPacketList==null || smallRedPacketList.isEmpty())
//                     return ResultsVoUtils.fail();
//
//                 int smallSize= smallRedPacketList.size();
//
//                 if(smallSize==1)
//                 {
//                     //大红包就剩一个小红包，取了之后这个大红包就没了
//                     //大红包状态设置成为结束
//                     bigredPacket.setOver(true);
//                     //被其他请求抢走了
//
//                     if(!redisService.delete(ListKey.getListKey,bigredPacket.getRed_id()))
//                         return ResultsVoUtils.fail();
//                 }
//
//                 int theexpection=smallRedPacketList.size();
//
//                  smallRedPacket= smallRedPacketList.remove(0);
//
//                 if(smallRedPacket==null)
//                     return ResultsVoUtils.fail();
//                 else {
//                     if(redisService.getArray(ListKey.getListKey,bigredPacket.getRed_id()).size()==theexpection)
//                     {
//                         redisService.set(ListKey.getListKey,bigredPacket.getRed_id(),smallRedPacketList);
//                         break;
//                     }
//                 }
//
//             }while (true);


             //这里用事务去取小红包，这样可以避免写的场景

        SmallRedPacket smallRedPacket= redisService.tget(ListKey.getListKey,bigredPacket.getRed_id(),SmallRedPacket.class);
        //取完了
        if(smallRedPacket==null)
        {
            bigredPacket.setOver(true);

            return ResultsVoUtils.fail();
        }else
        {
            //事务增长用户的余额
           while (true)
           {
               boolean result= redisService.blanceset(UserKey.getById,userId,smallRedPacket.getAmmount());
               if(result)
                   break;
           }
        }
        bigredPacket.getUserList().add(userId);

        //用户账户扔进队列
        UserBalanceTaskMessage userBalanceTaskMessage =new UserBalanceTaskMessage();
        userBalanceTaskMessage.setUserId(userId);
        userBalanceTaskMessage.setBalanceChange(smallRedPacket.getAmmount());
        taskSender.sendUserBalanceInfoService(userBalanceTaskMessage);

        //小红包信息扔进队列
        taskSender.SendSmallRedPacket(smallRedPacket);
        return ResultsVoUtils.success(redisService.get(UserKey.getById,userId,Double.class));
    }
}
