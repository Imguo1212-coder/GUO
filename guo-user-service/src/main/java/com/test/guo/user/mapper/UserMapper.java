package com.test.guo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.guo.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}