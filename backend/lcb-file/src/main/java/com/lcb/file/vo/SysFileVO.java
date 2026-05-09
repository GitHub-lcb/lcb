package com.lcb.file.vo;

import com.lcb.file.domain.SysFile;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysFileVO {
    private Long id;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private String url;
    private String storageType;
    private LocalDateTime createTime;

    public static SysFileVO fromEntity(SysFile file) {
        if (file == null) return null;
        SysFileVO vo = new SysFileVO();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setOriginalName(file.getOriginalName());
        vo.setFileSize(file.getFileSize());
        vo.setFileType(file.getFileType());
        vo.setUrl(file.getUrl());
        vo.setStorageType(file.getStorageType());
        vo.setCreateTime(file.getCreateTime());
        return vo;
    }
}
