package com.lcb.monitor.vo;

import com.lcb.monitor.domain.SysAuditLog;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysAuditLogVO {
    private Long id;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long duration;
    private Integer status;
    private LocalDateTime createTime;

    public static SysAuditLogVO fromEntity(SysAuditLog log) {
        if (log == null) return null;
        SysAuditLogVO vo = new SysAuditLogVO();
        vo.setId(log.getId());
        vo.setUsername(log.getUsername());
        vo.setOperation(log.getOperation());
        vo.setMethod(log.getMethod());
        vo.setParams(log.getParams());
        vo.setIp(log.getIp());
        vo.setDuration(log.getDuration());
        vo.setStatus(log.getStatus());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
