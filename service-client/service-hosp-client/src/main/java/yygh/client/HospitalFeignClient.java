package yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import yygh.vo.hosp.ScheduleOrderVo;
import yygh.vo.order.SignInfoVo;

//Feign接口类实现调用
@FeignClient(value = "service-hosp",path = "/api/hosp/hospital/inner")
@Service
public interface HospitalFeignClient {

    //根据排班id获取预约下单数据
    @GetMapping("/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);


    //获取医院签名信息
    @GetMapping("/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}
