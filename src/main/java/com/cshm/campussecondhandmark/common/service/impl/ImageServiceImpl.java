package com.cshm.campussecondhandmark.common.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${cshm.upload.path:${user.dir}/upload/}")
    private String uploadBasePath;

    @Value("${cshm.upload.public-prefix}")
    private String uploadPublicPrefix;

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BaseException("文件不能为空");
        }

        // 2. 确保基础目录存在
        File dir = new File(uploadBasePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 获取原始文件名并生成新文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        // 4. 保存文件到目标路径
        File targetFile = new File(dir, newFileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        // 5. 返回 Web 访问路径 (而不是服务器物理路径)
        return uploadPublicPrefix + "/upload/" + newFileName;
    }
}
