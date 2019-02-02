package com.beta.service;

import com.beta.aqmp.UserBalanceTaskMessage;

public interface UserTransactionService {
    int writeIntoDataBase(UserBalanceTaskMessage userBalanceTaskMessage);
}
