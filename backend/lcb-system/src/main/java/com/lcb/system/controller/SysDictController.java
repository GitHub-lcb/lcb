package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysDictData;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.mapper.SysDictDataMapper;
import com.lcb.system.mapper.SysDictTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public SysDictController(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    @Operation(summary = "字典类型分页")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/page")
    public Result<Page<SysDictType>> typePage(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(dictTypeMapper.selectPage(new Page<>(page, pageSize), null));
    }

    @Operation(summary = "获取字典数据")
    @GetMapping("/data/{type}")
    public Result<List<SysDictData>> getData(@PathVariable String type) {
        return Result.ok(dictDataMapper.selectByType(type));
    }

    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody SysDictType dictType) {
        dictTypeMapper.insert(dictType);
        return Result.ok();
    }
}
