package com.test.guo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.test.guo.entity.User;
import com.test.guo.mapper.UserMapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Controller 调用 Service，Service 调用 Mapper
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 新增 */
    public User create(User user) {
        userMapper.insert(user);
        return user;
    }

    /** 删除 */
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    /** 修改 */
    public void update(Long id, User user) {
        user.setId(id);
        userMapper.updateById(user);  // MyBatis-Plus 自带
    }
    /** 查询 */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> list(String name) {
        // 没传 name 或为空，查全部
        if (name == null || name.trim().isEmpty()) {
            return userMapper.selectList(null);
        }

        // 有 name，模糊查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getName, name);  //引用 User 类的 getName 方法，表示「按 name 字段」，方法传进来的参数
        return userMapper.selectList(wrapper);
    }

}