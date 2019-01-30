package com.beta.exception;


import com.beta.Emuns.ChannelEmus;

public class ChannelNotFoundException extends RuntimeException{

    private Integer code;

    public ChannelNotFoundException(Integer code, String message)
    {
        super(message);
        this.code=code;
    }

    public ChannelNotFoundException(ChannelEmus channelEmus)
    {
        super(channelEmus.getMsg());
        this.code=channelEmus.getCode();
    }
}
