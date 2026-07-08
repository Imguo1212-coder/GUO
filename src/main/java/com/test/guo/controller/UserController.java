package com.test.guo.controller;

import org.springframework.web.bind.annotation.RequestParam;
import com.test.guo.entity.User;
import com.test.guo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对外提供 HTTP 接口
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 新增 */
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    /** 删除*/
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "删除成功";
    }


    /** 修改*/
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody User user) {
        userService.update(id, user);
        return "修改成功";
    }

    /** 查询单个 */
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /** 查询全部*/
    @GetMapping
    public List<User> list(@RequestParam(required = false) String name) {
        return userService.list(name);
    }
}