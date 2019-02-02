package com.beta.service;

import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;

public interface RedPacketDAOservice {
    BigredPacket findByRedId(String redid);
    BigredPacket save(BigredPacket bigredPacket);
    SmallRedPacket save(SmallRedPacket smallRedPacket);
    SmallRedPacket findbyRedidSmall(String redid);
}
