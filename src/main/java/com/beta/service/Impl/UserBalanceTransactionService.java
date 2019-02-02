package com.beta.service.Impl;

import com.beta.aqmp.UserBalanceTaskMessage;
import com.beta.mapper.UserBlanceMapper;
import com.beta.service.UserTransactionService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserBalanceTransactionService implements UserTransactionService {

    @Autowired
    private
    UserBlanceMapper userBlanceMapper;


    @Override
    public int writeIntoDataBase(UserBalanceTaskMessage userBalanceTaskMessage) {
        //todo 思路 首先获取版本号，获取到版本号之后使用CAS更新，之后看版本号对不对

        do{
            userBalanceTaskMessage.setVersion(userBlanceMapper.getVersion(userBalanceTaskMessage.getUserId()));
           if( userBlanceMapper.updateUserBalancceService(userBalanceTaskMessage.getBalanceChange(),userBalanceTaskMessage.getUserId(),userBalanceTaskMessage.getVersion()))
               break;
        }while (true);


        return 1;

    }
}
