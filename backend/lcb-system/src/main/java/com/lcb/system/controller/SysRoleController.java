package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysRole;
import com.lcb.system.dto.RoleMenuDTO;
import com.lcb.system.dto.SysRoleDTO;
import com.lcb.system.service.ISysRoleService;
import com.lcb.system.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

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
    public Result<Page<SysRoleVO>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysRole> entityPage = roleService.page(new Page<>(page, pageSize));
        Page<SysRoleVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(SysRoleVO::fromEntity)
            .collect(Collectors.toList()));
        return Result.ok(voPage);
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysRoleDTO dto) {
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setDataScope(dto.getDataScope());
        role.setStatus(dto.getStatus());
        roleService.save(role);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysRoleDTO dto) {
        SysRole role = new SysRole();
        role.setId(dto.getId());
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setDataScope(dto.getDataScope());
        role.setStatus(dto.getStatus());
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
        roleService.assignMenu(dto.getRoleId(), dto.getMenuIds());
        return Result.ok();
    }
}
