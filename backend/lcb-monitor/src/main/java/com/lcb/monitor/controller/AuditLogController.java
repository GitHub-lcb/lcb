package com.lcb.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.PageUtils;
import com.lcb.common.core.Result;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.service.IAuditLogService;
import com.lcb.monitor.vo.SysAuditLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审计日志")
@RestController
@RequestMapping("/api/monitor/audit-log")
public class AuditLogController {

    private final IAuditLogService auditLogService;

    public AuditLogController(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "审计日志分页")
    @SaCheckPermission("monitor:audit-log:list")
    @GetMapping("/page")
    public Result<Page<SysAuditLogVO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysAuditLog> entityPage = auditLogService.page(new Page<>(page, pageSize));
        return Result.ok(PageUtils.convert(entityPage, SysAuditLogVO::fromEntity));
    }
}
