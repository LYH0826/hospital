package yygh.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import yygh.service.FileService;
import yygh.utils.ConstantOssPropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    /*
    获取上传文件
    参考阿里云OSS的API文档：
    https://help.aliyun.com/document_detail/84781.html
     */
    @Override
    public String upload(MultipartFile file) {
        //初始化参数
        String endpoint = ConstantOssPropertiesUtils.ENDPOINT;
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRECT;
        String bucketName = ConstantOssPropertiesUtils.BUCKET;

        OSS ossClient = null;
        try {
            //创建OssClient实例
            ossClient = new OSSClientBuilder()
                    .build(endpoint, accessKeyId, accessKeySecret);
            //创建上传文件流
            InputStream inputStream = file.getInputStream();

            String fileName = file.getOriginalFilename();
            //为防止重名文件覆盖，使用uuid生成随机唯一值，添加到文件名称里面
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //注意不能将uuid拼接在文件名后面，会使得文件后缀错乱
            fileName = uuid + fileName;

            //根据当前日期创建多级目录，并将文件上传到目录中
            String dateUrl = new DateTime().toString("yyyy/MM/dd");
            fileName = dateUrl + "/" + fileName;

            //调用方法实现上传
            ossClient.putObject(bucketName, fileName, inputStream);

            //返回上传之后的文件路径
            //示例：https://lyh-yygh.oss-cn-guangzhou.aliyuncs.com/ptuyy.jpg
            return "https://" + bucketName + "." + endpoint + "/" + fileName;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return  null;
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        } finally {
            //关闭OssClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
