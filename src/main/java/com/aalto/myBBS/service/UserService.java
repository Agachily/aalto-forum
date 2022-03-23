package com.aalto.myBBS.service;

import com.aalto.myBBS.dao.UserMapper;
import com.aalto.myBBS.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}
