package com.beta.service.Impl;

import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import com.beta.service.ReadPacketService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class ReadServicePacketImpl implements ReadPacketService {


    @Override
    public ArrayList<SmallRedPacket> division(BigredPacket bigredPacket, int part) {

        ArrayList<SmallRedPacket> smallRedPacketArrayList =new ArrayList<>(part);
        for(int i=0;i<part;i++)
        {
            SmallRedPacket smallRedPacket =new SmallRedPacket();
            smallRedPacket.setAmmount(bigredPacket.getCount()/bigredPacket.getCount());
            smallRedPacket.setBigRedPacketBelong(bigredPacket.getRedid());
            smallRedPacket.setRedid(UUID.randomUUID().toString());
            smallRedPacketArrayList.add(smallRedPacket);
            smallRedPacket.setUserbelong("");

        }
        return smallRedPacketArrayList;
    }
}
