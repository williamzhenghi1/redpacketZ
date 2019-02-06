package com.beta.service;

import com.beta.pojo.User;

import java.util.List;

public interface UserService {
    User save(User user);
    List<User> findAll();

}
