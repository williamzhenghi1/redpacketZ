package com.beta.service.Impl;

import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import com.beta.repositry.RepositryService;
import com.beta.repositry.SmallRedPacketRepositry;
import com.beta.service.ReadPacketService;
import com.beta.service.RedPacketDAOservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RedPacketDaoServiceImpl implements RedPacketDAOservice {

    @Autowired
    RepositryService repositryService;

    @Autowired
    SmallRedPacketRepositry smallRedPacketRepositry;

    @Override
    public BigredPacket findByRedId(String redid) {
        return repositryService.findByRedid(redid);
    }

    @Override
    public BigredPacket save(BigredPacket bigredPacket) {
        return repositryService.save(bigredPacket);
    }

    @Override
    public SmallRedPacket save(SmallRedPacket smallRedPacket) {
        return smallRedPacketRepositry.save(smallRedPacket);
    }

    @Override
    public SmallRedPacket findbyRedidSmall(String redid) {
        return smallRedPacketRepositry.findByRedid(redid);
    }
}
