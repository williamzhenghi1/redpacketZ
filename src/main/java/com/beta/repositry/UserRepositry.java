package com.beta.repositry;

import com.beta.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface UserRepositry extends JpaRepository<User,Integer> {
    ArrayList<User> findAll();
}
