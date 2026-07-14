package com.test.guo.dept.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentCreateRequest {

    @NotBlank(message = "部门名称不能为空")
    private String name;

    @Size(max = 500, message = "部门描述不能超过 500 个字符")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
