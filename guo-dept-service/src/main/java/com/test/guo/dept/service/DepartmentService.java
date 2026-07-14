package com.test.guo.dept.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.guo.common.exception.BusinessException;
import com.test.guo.common.exception.ErrorCode;
import com.test.guo.dept.dto.DepartmentCreateRequest;
import com.test.guo.dept.dto.DepartmentUpdateRequest;
import com.test.guo.dept.entity.Department;
import com.test.guo.dept.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DepartmentCacheService departmentCacheService;

    public DepartmentService(DepartmentMapper departmentMapper,
                             DepartmentCacheService departmentCacheService) {
        this.departmentMapper = departmentMapper;
        this.departmentCacheService = departmentCacheService;
    }

    public Department create(DepartmentCreateRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCreateTime(LocalDateTime.now());

        int rows = departmentMapper.insert(department);
        if (rows == 0) {
            throw new BusinessException(
                    ErrorCode.DEPARTMENT_OPERATION_FAILED,
                    "部门新增失败"
            );
        }
        departmentCacheService.put(department);
        return department;
    }

    public void delete(Long id) {
        int rows = departmentMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(
                    ErrorCode.DEPARTMENT_NOT_FOUND,
                    "部门不存在，删除失败"
            );
        }
        departmentCacheService.delete(id);
    }

    public void update(Long id, DepartmentUpdateRequest request) {
        Department department = new Department();
        department.setId(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        int rows = departmentMapper.updateById(department);
        if (rows == 0) {
            throw new BusinessException(
                    ErrorCode.DEPARTMENT_NOT_FOUND,
                    "部门不存在，修改失败"
            );
        }
        departmentCacheService.delete(id);
    }

    public Department getById(Long id) {
        Department cached = departmentCacheService.get(id);
        if (cached != null) {
            return cached;
        }

        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        departmentCacheService.put(department);
        return department;
    }

    public IPage<Department> page(long page, long size) {
        Page<Department> departmentPage = new Page<>(page, size);
        return departmentMapper.selectPage(departmentPage, null);
    }

    public List<Department> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Department> hitMap = departmentCacheService.getAll(ids);
        List<Long> missIds = departmentCacheService.missIds(ids, hitMap);

        if (!missIds.isEmpty()) {
            List<Department> fromDb = departmentMapper.selectByIds(missIds);
            departmentCacheService.putAll(fromDb);
            for (Department department : fromDb) {
                if (department != null && department.getId() != null) {
                    hitMap.put(department.getId(), department);
                }
            }
        }

        List<Department> result = new ArrayList<>();
        for (Long id : ids) {
            Department department = hitMap.get(id);
            if (department != null) {
                result.add(department);
            }
        }
        return result;
    }
}
