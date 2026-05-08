package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.RoleMenuDTO;
import com.lcb.system.domain.SysRole;
import com.lcb.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final ISysRoleService roleService;

    public SysRoleController(ISysRoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "角色分页列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/page")
    public Result<Page<SysRole>> page(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(roleService.page(new Page<>(page, pageSize)));
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "分配角色菜单权限")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/menu")
    public Result<Void> assignMenu(@RequestBody RoleMenuDTO dto) {
        roleService.getBaseMapper().deleteById(dto.getRoleId());
        // 实际项目中应操作 t_sys_role_menu 表
        return Result.ok();
    }
}
