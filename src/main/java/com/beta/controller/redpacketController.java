package com.beta.controller;

import com.alibaba.fastjson.JSON;
import com.beta.Utils.ResultsVoUtils;
import com.beta.Vo.ResultsVo;

import com.beta.aqmp.TaskSender;
import com.beta.aqmp.UserBalanceTaskMessage;
import com.beta.mapper.UserBlanceMapper;
import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import com.beta.pojo.User;
import com.beta.redis.KeyPrefix;
import com.beta.redis.ListKey;
import com.beta.redis.RedisService;
import com.beta.redis.UserKey;
import com.beta.repositry.RepositryService;
import com.beta.repositry.UserRepositry;
import com.beta.service.*;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.hibernate.annotations.Synchronize;
import org.hibernate.resource.transaction.backend.jta.internal.synchronization.SynchronizationCallbackCoordinatorNonTrackingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    UserRepositry userRepositry;

    @Autowired
    RedisService redisService;


    @Autowired
    BalanceService balanceService;

    @Autowired
    ReadPacketService readPacketService;

    @Autowired
    TaskSender taskSender;

    @Autowired
    RepositryService repositryService;

    @Autowired
    UserTransactionService userTransactionService;

    @Autowired
    UserService userService;

    @Autowired
    UserBlanceMapper userBlanceMapper;

    @Autowired
    RedPacketDAOservice redPacketDAOservice;



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
        bigredPacket.setRedid(UUID.randomUUID().toString());
        bigredPacket.setAmmount(price);
        bigredPacket.setUserbelong(userId);
        bigredPacket.setCount(part);
        ArrayList<SmallRedPacket> smallRedPacketArrayList= readPacketService.division(bigredPacket,part);
        bigredPackets.add(bigredPacket);
        taskSender.SendBigRedPacket(bigredPacket);

        /*
            小红包被保存在redis里面，大红包保存在队列里面，
         */

//        redisService.set(ListKey.getListKey,bigredPacket.getRed_id(),smallRedPacketArrayList);

        //这里重写了，用事务保存在队列里面

        for(int i=0;i<smallRedPacketArrayList.size();i++)
        {
            boolean result=true;
           do{
               result= redisService.tset(ListKey.getListKey,bigredPacket.getRedid(),smallRedPacketArrayList.get(i));
           }while (!result);
//            taskSender.SendSmallRedPacket(smallRedPacketArrayList.get(i));
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
        userBalanceTaskMessage.setBalanceChange(-price);
        userBalanceTaskMessage.setUserId(userId);
        taskSender.sendUserBalanceInfoService(userBalanceTaskMessage);
        /*
            异步红包记录
         */


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

           //检测重复
        /*
            这里检测重复的时候要注意以下几点：
            如果没查到有相同的，要立刻插入一个红包，以防止执行后面事务的时候遇到相同的用户插进来
         */

        if(bigredPacket.getSmallRedPacketList().size()!=0)
            for(int i=0;i<bigredPacket.getSmallRedPacketList().size();i++)
            {
                if(bigredPacket.getSmallRedPacketList().get(i).getUserbelong().equals(userId))
                {
                    log.info("重复抢购");
                    return ResultsVoUtils.fail();
                }
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

        SmallRedPacket smallRedPacket= redisService.tget(ListKey.getListKey,bigredPacket.getRedid(),SmallRedPacket.class);


            //取完了
        if(smallRedPacket==null)
        {
            bigredPacket.setOver(true);

            return ResultsVoUtils.fail();
        }else
        {
            smallRedPacket.setUserbelong(userId);
            bigredPacket.getSmallRedPacketList().add(smallRedPacket);

            //事务增长用户的余额
           while (true)
           {
               boolean result= redisService.blanceset(UserKey.getById,userId,smallRedPacket.getAmmount());
               if(result)
                   break;
           }
        }
//        bigredPacket.getUserList().add(userId);

        //用户账户扔进队列
        UserBalanceTaskMessage userBalanceTaskMessage =new UserBalanceTaskMessage();
        userBalanceTaskMessage.setUserId(userId);
        userBalanceTaskMessage.setBalanceChange(smallRedPacket.getAmmount());
        taskSender.sendUserBalanceInfoService(userBalanceTaskMessage);


        //小红包信息扔进队列
        taskSender.SendSmallRedPacket(smallRedPacket);

        //直接找到自己的红包，更新下剩下的信息

        return ResultsVoUtils.success(redisService.get(UserKey.getById,userId,Double.class));
    }
    @GetMapping("test3")
    public ResultsVo jpatest()
    {

        UserBalanceTaskMessage userBalanceTaskMessage =new UserBalanceTaskMessage();
        userBalanceTaskMessage.setUserId("1");
        userBalanceTaskMessage.setBalanceChange(20.0);
        userBalanceTaskMessage.setVersion(1);
//        userTransactionService.writeIntoDataBase(userBalanceTaskMessage);
        taskSender.sendUserBalanceInfoService(userBalanceTaskMessage);
        return ResultsVoUtils.success("success");
    }

    @GetMapping("test4")
    public String test4()
    {
        SmallRedPacket smallRedPacket =new SmallRedPacket();
        smallRedPacket.setRedid("3");
        smallRedPacket.setAmmount(100.0);
        smallRedPacket.setUserbelong("123");
        smallRedPacket.setBigRedPacketBelong("123231");

        BigredPacket bigredPacket =new BigredPacket();
        bigredPacket.setOver(true);
        bigredPacket.setCount(10);
        bigredPacket.setAmmount(200);
        bigredPacket.setRedid("1231241");
        bigredPacket.setUserbelong("qwewa");
        taskSender.SendSmallRedPacket(smallRedPacket);
        taskSender.SendBigRedPacket(bigredPacket);

//        redPacketDAOservice.save(bigredPacket);
        return "success";
    }

    //生成数据库测试用数据

    @GetMapping("/genData")
    ResultsVo genData() throws IOException {
        //生成的数据
        File csv = new File("D:/makeRedPacket.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(csv,true));
        //取的数据
        File getPacket = new File("D:/getRedPacket.csv"); // CSV数据文件
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(getPacket,true)); // 附加

        for(int i=0;i<1000;i++)
        {
            User user = new User();
            user.setBalance(new Random().nextInt(1000));
            user.setUserid(UUID.randomUUID().toString());
//            user.setVersion(0);
            userService.save(user);
            //写进redis
//            redisService.set(UserKey.getById,user.getUserid(),user.getBalance());
            //生成发送取的csv文件

            if(new Random().nextBoolean())
            {
//           File csv = new File("D:/makeRedPacket.csv"); // CSV数据文件
//                BufferedWriter bw = new BufferedWriter(new FileWriter(csv,true)); // 附加
                // 添加新的数据行
                bw.write(user.getVersion()+","+new Random().nextInt(500)+","+"1,"+"10");
                bw.newLine();

            }

//            BufferedWriter bw1 = new BufferedWriter(new FileWriter(getPacket,true)); // 附加
            // 添加新的数据行
            bw1.write(user.getUserid()+","+"1");
            bw1.newLine();

        }

        //randomGet Packet
//        File getPacket = new File("D:/getRedPacket.csv"); // CSV数据文件


        bw.close();
        bw1.close();

        return ResultsVoUtils.success("success");

    }

    @GetMapping("/test5")
    public String test5()
    {
        User user =new User();
        user.setUserid("123xx");
        user.setBalance(200.0);

        userRepositry.save(user);
        return "success";
    }

    /**
     * 这里暂时不写进postConstruct 避免调试加载时间过长，后期部署可以改进
     * @return
     */

    @GetMapping("/loadRedis")
    public String loadRedis()
    {

        List<User> users = userRepositry.findAll();

        int count =0;
        redisService.setSql(users);
        //写redis

        return "success";
    }




}
