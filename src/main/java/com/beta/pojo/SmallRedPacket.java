package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

//@Entity
@Data
public class SmallRedPacket {
//
//    @Id
//    @Column(name = "id")
    private int id;
    private String userbelong;
    private String bigRedPacketBelong;
    private double ammount;
    private String red_id;
}
