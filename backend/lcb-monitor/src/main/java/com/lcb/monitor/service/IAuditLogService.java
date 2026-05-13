package com.lcb.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.monitor.domain.SysAuditLog;

public interface IAuditLogService extends IService<SysAuditLog> {
    String resolveUsername(Long userId);
}
