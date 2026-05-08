package com.lcb.monitor.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_sys_audit_log")
public class SysAuditLog {
    private Long id;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long duration;
    private Integer status;
    private LocalDateTime createTime;
}
