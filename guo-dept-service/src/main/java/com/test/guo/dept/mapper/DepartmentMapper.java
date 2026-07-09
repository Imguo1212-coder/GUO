package com.test.guo.dept.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.guo.dept.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
