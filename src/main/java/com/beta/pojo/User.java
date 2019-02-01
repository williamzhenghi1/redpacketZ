package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class User {

    @Id
    @Column(name = "userid")
    private int userid;
    private double balance;
}
