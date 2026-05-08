package com.lcb.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.file.domain.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService extends IService<SysFile> {
    SysFile upload(MultipartFile file);
}
