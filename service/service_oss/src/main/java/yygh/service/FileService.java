package yygh.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    //获取上传文件
    String upload(MultipartFile file);
}
