package com.sky.image;

import org.apache.ibatis.annotations.Mapper;

import java.io.InputStream;


public interface ImageMapper {
    String upload(InputStream input, String fileType);
}
