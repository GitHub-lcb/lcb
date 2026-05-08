package com.lcb.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.mapper.SysAuditLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审计日志")
@RestController
@RequestMapping("/api/monitor/audit-log")
public class AuditLogController {

    private final SysAuditLogMapper auditLogMapper;

    public AuditLogController(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Operation(summary = "审计日志分页")
    @SaCheckPermission("monitor:audit-log:list")
    @GetMapping("/page")
    public Result<Page<SysAuditLog>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(auditLogMapper.selectPage(new Page<>(page, pageSize), null));
    }
}
