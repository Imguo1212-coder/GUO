package com.test.guo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * @TableName("user")：对应数据库 user 表
 */
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String email;

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Integer getAge() {
        return age;
    }
    public String getEmail() {
        return email;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @TableField("create_time")
    private LocalDateTime createTime;

    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @TableField("department_id")
    private Long departmentId;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

}