package com.sky.image;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/common")
@RestController
@Slf4j
public class ImageController {
    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file){
        String imagePath=imageService.saveImg(file);
        return Result.success(imagePath);
    }

}

