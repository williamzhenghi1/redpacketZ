package com.beta.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
public class User {

    private int userid;
    private long balance;
}
