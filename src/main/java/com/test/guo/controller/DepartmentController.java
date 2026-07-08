package com.test.guo.controller;

import com.test.guo.entity.Department;
import com.test.guo.service.DepartmentService;
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
    public Department create(@RequestBody Department department) {
        return departmentService.create(department);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        departmentService.delete(id);
        return "删除成功";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Department department) {
        departmentService.update(id, department);
        return "修改成功";
    }

    @GetMapping("/{id}")
    public Department getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @GetMapping
    public List<Department> list() {
        return departmentService.list();
    }
}
