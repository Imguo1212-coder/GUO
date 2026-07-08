package com.test.guo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.guo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper继承 BaseMapper<User> 后，自动拥有增删改查方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}