package com.beta.mapper;

import com.beta.pojo.SmallRedPacket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface RepacketMapper {

    @Insert("insert into SmallRedPacket")
    public SmallRedPacket smallRedPacketWrite(SmallRedPacket smallRedPacket);
}
