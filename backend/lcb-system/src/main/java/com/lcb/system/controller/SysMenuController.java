package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysMenu;
import com.lcb.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    private final ISysMenuService menuService;

    public SysMenuController(ISysMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "菜单树")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        List<SysMenu> all = menuService.list();
        return Result.ok(buildTree(all, 0L));
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return Result.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:remove")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.ok();
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
            .filter(m -> m.getParentId() != null && m.getParentId().equals(parentId))
            .peek(m -> m.setChildren(buildTree(all, m.getId())))
            .collect(Collectors.toList());
    }
}
