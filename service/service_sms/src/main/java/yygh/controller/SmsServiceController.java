package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yygh.result.Result;
import yygh.service.SmsService;
import yygh.utils.RandomUtil;

import java.util.concurrent.TimeUnit;

@Api(tags = "短信服务：SmsServiceController")
@RestController
@RequestMapping("/api/sms")
public class SmsServiceController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @ApiOperation("发送短信验证码")
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable("phone") String phone){
        //从Redis中获取短信验证码，如果获取得到，则返回ok
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)){
            return Result.ok();
        }

        //如果获取不到，则生成验证码
        code = RandomUtil.getSixBitRandom();
        //调用service方法，通过整合短信服务进行发送
        boolean isSend = smsService.send(phone,code);
        //生成的验证码定义到reids里面，并设置键有效时长
        if (isSend){
            //分别设置key,value,过期时间，时间单位
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return Result.ok();
        }else {
            return Result.fail().message("发送短信失败！");
        }
    }
}
