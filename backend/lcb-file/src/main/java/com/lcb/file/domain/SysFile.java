package com.lcb.file.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_file")
public class SysFile extends BaseEntity {
    private Long id;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private String url;
    private String storageType;
}
