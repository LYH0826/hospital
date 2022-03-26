package yygh.service;

import yygh.vo.msm.MsmVo;

import java.util.Map;

public interface SmsService {
    //发送短信验证码，并判断是否发送成功
    boolean send(String phone, String code);

    //发送短信提醒预约成功
    boolean sendInfo(String phone, Map<String, Object> param);

    //mq使用发送短信
    boolean send(MsmVo msmVo);
}
