package com.beta.aqmp;

import com.beta.pojo.BigredPacket;
import com.beta.pojo.SmallRedPacket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@Data
public class RedPacketLogTaskMessage {
    private ArrayList<SmallRedPacket> smallRedPackets;
    private String bigRedPacketId;
    private double Amount;
    private int part;
    private String owner;
    private String redPacketId;
}
