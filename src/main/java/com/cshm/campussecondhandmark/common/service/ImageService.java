package com.cshm.campussecondhandmark.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String uploadFile(MultipartFile file);
}
