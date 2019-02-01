package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
//
//@Entity
@Data
public class BigredPacket {

//    @Id
//    @Column(name="id")
    private int id;
    private double ammount;
    private int count;
    private String userbelong;
    private String red_id;
    private boolean isOver;
    private List<String> userList=new ArrayList<>(10);
}
