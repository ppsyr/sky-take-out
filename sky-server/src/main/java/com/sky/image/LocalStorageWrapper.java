package com.sky.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
@Slf4j
@ConditionalOnExpression(value = "#{'local'.equals(environment.getProperty('image.oss.type'))}")
public class LocalStorageWrapper implements ImageMapper {

    @Autowired
    private ImageProperties imageProperties;
    //随机数用于生成临时文件名
    private Random random;

    public LocalStorageWrapper() {
        random = new Random();
    }


    @Override
    public String upload(InputStream input, String fileType) {
        try {
            //文件存储路径
            String path=imageProperties.getAbsTmpPath()+imageProperties.getWebImgPath();
            //获取文件临时名
            String fileName=genTmpFileName();
            //保存文件
            File file = saveFileByStream(input, path, fileName, fileType);
            return imageProperties.buildImgUrl(imageProperties.getWebImgPath()+file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取文件临时名称
     * 当前时间戳+随机数
     * @return
     */
    private String genTmpFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS")) + "_" + random.nextInt(100);
    }

    /**
     * 保存文件
     * @param stream
     * @param path
     * @param filename
     * @param fileType
     * @return
     * @throws FileNotFoundException
     */
    private File saveFileByStream(InputStream stream, String path, String filename, String fileType) throws FileNotFoundException {
        //当目录不存在时，创建目录
        if (path!=null&&path.length()>0) {
            mkDir(new File(path));
        }
        //文件路径
        String tempAbsFile = path + "/" + filename + "." + fileType;
        //将文件写入指定路径中
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        File var6;
        try {
            inputStream = new BufferedInputStream(stream);
            outputStream = new BufferedOutputStream(new FileOutputStream(tempAbsFile));
            int len = inputStream.available();
            if (len <= 4096) {
                byte[] bytes = new byte[len];
                inputStream.read(bytes);
                outputStream.write(bytes);
            } else {
                byte[] bytes = new byte[4096];
                int byteCount;
                while((byteCount = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, byteCount);
                }
            }
            var6 = new File(filename,fileType);
            return var6;
        } catch (Exception var16) {
            log.error("save stream into file error! filename: {} e: {}", tempAbsFile, var16);
            var6=null;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException var15) {
                log.error("close stream error!", var15);

            }
        }
        return var6;
    }

    /**
     * 递归创建目录，并设置目录权限
     * @param path
     * @throws FileNotFoundException
     */
    private void mkDir(File path) throws FileNotFoundException {
        if (path.getParentFile() == null) {
            path = path.getAbsoluteFile();
        }

        if (path.getParentFile() != null) {
            if (path.getParentFile().exists()) {
                modifyFileAuth(path);
                if (!path.exists() && !path.mkdir()) {
                    throw new FileNotFoundException();
                }
            } else {
                mkDir(path.getParentFile());
                modifyFileAuth(path);
                if (!path.exists() && !path.mkdir()) {
                    throw new FileNotFoundException();
                }
            }

        }
    }

    /**
     * 设置目录或文件的权限
     * @param file
     */
    private void modifyFileAuth(File file) {
        boolean ans = file.setExecutable(true, false);
        ans = file.setReadable(true, false) && ans;
        ans = file.setWritable(true, false) && ans;
        if (log.isDebugEnabled()) {
            log.debug("create file auth : {}", ans);
        }

    }
}
