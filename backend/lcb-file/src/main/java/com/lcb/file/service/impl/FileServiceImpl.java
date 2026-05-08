package com.lcb.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.file.domain.SysFile;
import com.lcb.file.mapper.SysFileMapper;
import com.lcb.file.service.IFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements IFileService {

    private final String uploadDir = "upload";

    @Override
    public SysFile upload(MultipartFile file) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storageName = UUID.randomUUID().toString().replace("-", "") + "_" + file.getOriginalFilename();
        String relativePath = dateDir + "/" + storageName;

        try {
            Path targetPath = Paths.get(uploadDir, relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());

            SysFile sysFile = new SysFile();
            sysFile.setFileName(storageName);
            sysFile.setOriginalName(file.getOriginalFilename());
            sysFile.setFileSize(file.getSize());
            sysFile.setFileType(file.getContentType());
            sysFile.setUrl("/" + uploadDir + "/" + relativePath);
            sysFile.setStorageType("local");
            save(sysFile);
            return sysFile;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
