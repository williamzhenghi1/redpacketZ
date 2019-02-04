package com.beta.pojo;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private int id;
    private double balance;
    private int version;
    private String userid;
}
