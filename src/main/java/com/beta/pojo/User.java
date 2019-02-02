package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class User {

    @Id
    @Column(name = "id")
    private int id;
    private double balance;
    private int version;
    private String userid;
}
