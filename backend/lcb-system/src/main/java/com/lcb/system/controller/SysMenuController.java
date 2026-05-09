package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysMenu;
import com.lcb.system.dto.SysMenuDTO;
import com.lcb.system.service.ISysMenuService;
import com.lcb.system.vo.SysMenuVO;
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
    public Result<List<SysMenuVO>> tree() {
        List<SysMenu> all = menuService.list();
        return Result.ok(buildTree(all, 0L));
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(dto.getMenuName());
        menu.setPermission(dto.getPermission());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setIcon(dto.getIcon());
        menu.setParentId(dto.getParentId());
        menu.setSort(dto.getSort());
        menu.setMenuType(dto.getMenuType());
        menu.setStatus(dto.getStatus());
        menuService.save(menu);
        return Result.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @PutMapping
    public Result<Void> edit(@RequestBody SysMenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setId(dto.getId());
        menu.setMenuName(dto.getMenuName());
        menu.setPermission(dto.getPermission());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setIcon(dto.getIcon());
        menu.setParentId(dto.getParentId());
        menu.setSort(dto.getSort());
        menu.setMenuType(dto.getMenuType());
        menu.setStatus(dto.getStatus());
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

    private List<SysMenuVO> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
            .filter(m -> m.getParentId() != null && m.getParentId().equals(parentId))
            .map(m -> {
                SysMenuVO vo = SysMenuVO.fromEntity(m);
                vo.setChildren(buildTree(all, m.getId()));
                return vo;
            })
            .collect(Collectors.toList());
    }
}
