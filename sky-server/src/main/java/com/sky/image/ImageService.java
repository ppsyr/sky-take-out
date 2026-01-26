package com.sky.image;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String saveImg(MultipartFile file);
}
