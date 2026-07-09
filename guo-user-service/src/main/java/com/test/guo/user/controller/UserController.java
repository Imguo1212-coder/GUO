package com.test.guo.user.controller;

import com.test.guo.user.entity.User;
import com.test.guo.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "删除成功";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody User user) {
        userService.update(id, user);
        return "修改成功";
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<User> list(@RequestParam(required = false) String name) {
        return userService.list(name);
    }
}