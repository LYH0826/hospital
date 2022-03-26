package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yygh.service.FileService;
import yygh.result.Result;

@Api(tags = "OSS文件传输接口：FileApiController")
@RestController
@RequestMapping("/api/oss/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    @ApiOperation("上传文件到阿里云oss")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        //获取上传文件
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
