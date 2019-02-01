package com.beta.aqmp;

import com.beta.pojo.User;
import lombok.Data;



/**
 *
 */
@Data
public class UserBalanceTaskMessage {

    //todo 这里可以完善金额、时间戳

    private String userId;
    private double balanceChange;

}
