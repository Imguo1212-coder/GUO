package com.test.guo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

    @TableName("department")
    public class Department {

        @TableId(type = IdType.AUTO)
        private Long id;
        private String name;
        private String description;

        @TableField("create_time")
        private LocalDateTime createTime;

        public Long getId() {
            return id;
        }
        public String getDescription() {
            return description;
        }
        public String getName() {
            return name;
        }
        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
