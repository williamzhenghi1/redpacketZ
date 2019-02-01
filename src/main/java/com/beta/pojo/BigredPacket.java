package com.beta.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
//
//@Entity
@Data
@Entity
public class BigredPacket {


    @Id
    @Column(name = "id")
    private int id;
    private double ammount;
    private int count;
    private String userbelong;
    private String redid;
    private boolean isOver;
    @OneToMany(cascade = CascadeType.ALL,targetEntity = SmallRedPacket.class)
    @JoinColumn(name = "redid")
    private List<SmallRedPacket> smallRedPacketList=new ArrayList<>(10);

}
