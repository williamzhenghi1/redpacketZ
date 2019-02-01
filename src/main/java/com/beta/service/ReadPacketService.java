package com.beta.service;

import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;

import java.util.ArrayList;

public interface ReadPacketService {

    //TODo 红包被拆分成N份

    ArrayList<SmallRedPacket> division(BigredPacket bigredPacket,int part);

    //todo 大红包小红包分别写进redis
    


}
