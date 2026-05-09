package com.lcb.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.file.domain.SysFile;
import com.lcb.file.service.IFileService;
import com.lcb.file.vo.SysFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件")
    @SaCheckPermission("system:file:add")
    @PostMapping("/upload")
    public Result<SysFileVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(SysFileVO.fromEntity(fileService.upload(file)));
    }

    @Operation(summary = "文件列表")
    @SaCheckPermission("system:file:list")
    @GetMapping("/page")
    public Result<Page<SysFileVO>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysFile> entityPage = fileService.page(new Page<>(page, pageSize));
        Page<SysFileVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(SysFileVO::fromEntity)
            .collect(Collectors.toList()));
        return Result.ok(voPage);
    }

    @Operation(summary = "删除文件")
    @SaCheckPermission("system:file:remove")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.removeById(id);
        return Result.ok();
    }
}
