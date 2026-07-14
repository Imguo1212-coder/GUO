package com.test.guo.dept.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.test.guo.common.result.Result;
import com.test.guo.dept.dto.DepartmentCreateRequest;
import com.test.guo.dept.dto.DepartmentUpdateRequest;
import com.test.guo.dept.entity.Department;
import com.test.guo.dept.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public Result<Department> create(
            @Valid @RequestBody DepartmentCreateRequest request) {
        return Result.success(departmentService.create(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        departmentService.update(id, request);
        return Result.success();
    }
    @GetMapping("/batch")
    public Result<List<Department>> listByIds(@RequestParam List<Long> ids) {
        return Result.success(departmentService.listByIds(ids));
    }
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    @GetMapping
    public Result<IPage<Department>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(departmentService.page(page, size));
    }
}
