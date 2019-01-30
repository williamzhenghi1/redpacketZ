package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class User {
    @Column(name = "userid")
    @Id
    private int userid;
    private long balance;
}
