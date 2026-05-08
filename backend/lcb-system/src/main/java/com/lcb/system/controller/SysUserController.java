package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysUser;
import com.lcb.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final ISysUserService userService;

    public SysUserController(ISysUserService userService) {
        this.userService = userService;
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
}
