package com.beta.service;

import com.beta.pojo.User;

public interface BalanceService {

    double getUserBalance(String id);


    double balanceChange(String id,double balance);
}
