package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysUser;
import com.lcb.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;

    public SysUserController(ISysUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "用户分页列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(userService.page(new Page<>(page, pageSize)));
    }

    @Operation(summary = "用户详情")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:edit")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.fail("密码不能为空");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.ok();
    }
}
