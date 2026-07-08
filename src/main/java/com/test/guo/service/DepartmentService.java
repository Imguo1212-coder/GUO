package com.test.guo.service;

import com.test.guo.entity.Department;
import com.test.guo.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;
    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public Department create(Department department) {
        departmentMapper.insert(department);
        return department;
    }

    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }

    public void update(Long id, Department department) {
        department.setId(id);
        departmentMapper.updateById(department);
    }

    public Department getById(Long id) {
        return departmentMapper.selectById(id);
    }

    public List<Department> list() {
        return departmentMapper.selectList(null);
    }
}
