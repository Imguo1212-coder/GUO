package com.test.guo.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.test.guo.user.entity.User;
import com.test.guo.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User create(User user) {
        userMapper.insert(user);
        return user;
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public void update(Long id, User user) {
        user.setId(id);
        userMapper.updateById(user);
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> list(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userMapper.selectList(null);
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getName, name);
        return userMapper.selectList(wrapper);
    }
}