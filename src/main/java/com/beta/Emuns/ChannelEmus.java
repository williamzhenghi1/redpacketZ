package com.beta.Emuns;

import lombok.Getter;

import static com.beta.Const.StautsConst.CHANNEL_NOT_FOUND;


@Getter
public enum ChannelEmus {

    Success(0, "Success"),
    NOT_FOUNT_Faild(1,CHANNEL_NOT_FOUND);
    private Integer code;
    private String Msg;

    ChannelEmus(Integer code, String msg) {
        this.code = code;
        this.Msg = msg;
    }
}
