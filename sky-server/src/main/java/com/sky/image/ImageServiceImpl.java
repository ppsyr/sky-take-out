package com.sky.image;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private ImageMapper imageMapper;

    Set<MediaType> STATIC_IMG_TYPE = new HashSet<>(Arrays.asList(MediaType.ImagePng, MediaType.ImageJpg, MediaType.ImageWebp, MediaType.ImageGif));
    /**
     * 图片缓存，防止重复上传相同图片
     */
    private Cache<String, String> imgReplaceCache = CacheBuilder
            .newBuilder()
            .maximumSize(300)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public String saveImg(MultipartFile file){
        if (file==null) return "文件为空";
        //验证图片格式
        String fileType = validateStaticImg(file.getContentType());
        if (fileType==null) return "文件格式错误";
        try {
            //获取图像摘要，根据摘要确定缓存中是否已经包含图像
            String digest = calculateSHA256(file.getInputStream());
            String ans = imgReplaceCache.getIfPresent(digest);
            if (ans==null||ans==""){
                ans = imageMapper.upload(file.getInputStream(), fileType);
                imgReplaceCache.put(digest, ans);
            }
            return ans;
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算图片摘要
     * @param inputStream
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private String calculateSHA256(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        //将inputStream转换为ByteArrayInputStream
        InputStream newInputStream = null;
        if (!(inputStream instanceof ByteArrayInputStream)){
            try(ByteArrayOutputStream bos=new ByteArrayOutputStream()){
                BufferedInputStream br=new BufferedInputStream(inputStream);
                byte[] b=new byte[1024];
                for (int c;(c=br.read(b))!=-1;){
                    bos.write(b,0,c);
                }
                //主动告知回收
                b=null;
                br.close();
                inputStream.close();
                newInputStream=new ByteArrayInputStream(bos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            newInputStream=(ByteArrayInputStream) inputStream;
        }
        //读取InputStream并更新到MessageDigest上
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        byte[] buffer=new byte[1024];
        int bytesRead;
        while ((bytesRead = newInputStream.read(buffer)) != -1) {
            instance.update(buffer, 0, bytesRead);
        }
        //获取摘要并将其转换为十六进制字符串
        byte[] digest = instance.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        newInputStream.reset();
        return hexString.toString();

    }
    /**
     * 图片格式校验
     *
     * @param mime
     * @return
     */
    private String validateStaticImg(String mime) {
        if ("svg".equalsIgnoreCase(mime)) {
            // fixme 上传文件保存到服务器本地时，做好安全保护, 避免上传了要给攻击性的脚本
            return "svg";
        }

        if (mime.contains("jpg")) {
            mime = mime.replace("jpg", "jpeg");
        }
        for (MediaType type : STATIC_IMG_TYPE) {
            if (type.getMime().equals(mime)) {
                return type.getExt();
            }
        }
        return null;
    }
}
