package com.lcb.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.PageUtils;
import com.lcb.common.core.Result;
import com.lcb.generator.service.GeneratorService;
import com.lcb.generator.vo.GenTableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "代码生成")
@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final GeneratorService generatorService;

    public GeneratorController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @Operation(summary = "数据库表列表")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/table/page")
    public Result<Page<GenTableVO>> page(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(PageUtils.convert(
            generatorService.selectTablePage(new Page<>(page, pageSize)),
            GenTableVO::fromEntity));
    }

    @Operation(summary = "获取表列信息")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/table/{tableId}/columns")
    public Result<List<Map<String, Object>>> columns(@PathVariable Long tableId) {
        return Result.ok(generatorService.getDbColumns(
            generatorService.getTableById(tableId).getTableName()));
    }

    @Operation(summary = "生成代码到对应目录")
    @SaCheckPermission("generator:code:generate")
    @PostMapping("/code/{tableId}")
    public Result<Void> generate(@PathVariable Long tableId) {
        generatorService.generateCode(tableId);
        return Result.ok();
    }
}
