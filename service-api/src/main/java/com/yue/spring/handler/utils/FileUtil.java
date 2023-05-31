package com.yue.spring.handler.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.yue.spring.Exception.YueException;
import com.yue.spring.config.OSSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.util.UUID;
public class FileUtil {

    public static String fileUpload(MultipartFile file) throws IOException {
        OSS oss=new OSSClientBuilder().build(OSSConfig.END_POINT,OSSConfig.ACCESS_KEY_ID,OSSConfig.ACCESS_KEY_SECRET);
        if(file.getSize()>32505856)throw new YueException("文件大小不能超过512M");
        //获取文件名
        String fileName=file.getOriginalFilename();
        //断言fileName不为空
        assert fileName != null;
        //截取后后缀名
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //通过UUID和后缀名得到新文件名 防止重复名
        String newFileName= UUID.randomUUID().toString().replace("-","")+suffix;
        try{
            InputStream inputStream = file.getInputStream();
            ObjectMetadata objectMetadata=new ObjectMetadata();
            objectMetadata.setContentType(getContentType(newFileName.substring(newFileName.lastIndexOf("."))));
            //根据日期分组
            //Date dateTime=new DateTime().toString("yyyy/MM/dd")
            newFileName=OSSConfig.PREFIX+newFileName;
            oss.putObject(OSSConfig.BUCKET_NAME,newFileName,inputStream,objectMetadata);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            oss.shutdown();
        }
        return newFileName;
    }


    public static String getContentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase(".jpeg") ||
                FilenameExtension.equalsIgnoreCase(".jpg") ||
                FilenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpg";
        }
        if (FilenameExtension.equalsIgnoreCase(".html")) {
            return "text/html";
        }

        if (FilenameExtension.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        if (FilenameExtension.equalsIgnoreCase(".vsd")) {
            return "application/vnd.visio";
        }
        if (FilenameExtension.equalsIgnoreCase(".pdf")) {
            return "application/pdf";
        }
        if (FilenameExtension.equalsIgnoreCase(".pptx") ||
                FilenameExtension.equalsIgnoreCase(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (FilenameExtension.equalsIgnoreCase(".docx") ||
                FilenameExtension.equalsIgnoreCase(".doc")) {
            return "application/msword";
        }
        if (FilenameExtension.equalsIgnoreCase(".xml")) {
            return "text/xml";
        }
        return "image/jpg";
    }
}
