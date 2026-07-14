package com.test.guo.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.test.guo.common.result.Result;
import com.test.guo.user.dto.UserCreateRequest;
import com.test.guo.user.dto.UserUpdateRequest;
import com.test.guo.user.entity.User;
import com.test.guo.user.service.UserService;
import com.test.guo.user.vo.UserVO;
import jakarta.validation.Valid;
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
    public Result<User> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getByIdWithDeptName(id));
    }

    @GetMapping
    public Result<IPage<UserVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") long page,   // 第1页
            @RequestParam(defaultValue = "10") long size) { // 每页10条
        return Result.success(userService.pageWithDeptName(name, page, size));
    }
}
