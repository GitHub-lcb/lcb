package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysUser;
import com.lcb.system.dto.PasswordResetDTO;
import com.lcb.system.dto.SysUserDTO;
import com.lcb.system.service.ISysUserService;
import com.lcb.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

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
    public Result<Page<SysUserVO>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysUser> entityPage = userService.page(new Page<>(page, pageSize));
        Page<SysUserVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(SysUserVO::fromEntity)
            .collect(Collectors.toList()));
        return Result.ok(voPage);
    }

    @Operation(summary = "用户详情")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUserVO> get(@PathVariable Long id) {
        return Result.ok(SysUserVO.fromEntity(userService.getById(id)));
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysUserDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
        userService.save(user);
        return Result.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysUserDTO dto) {
        if (dto.getPassword() != null && dto.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        SysUser user = new SysUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
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
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordResetDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userService.updateById(user);
        return Result.ok();
    }
}
