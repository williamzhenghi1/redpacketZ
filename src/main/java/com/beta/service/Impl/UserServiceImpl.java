package com.beta.service.Impl;

import com.beta.pojo.User;
import com.beta.repositry.UserRepositry;
import com.beta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepositry userRepositry;

    @Override
    public User save(User user) {
        return userRepositry.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepositry.findAll();
    }
}
