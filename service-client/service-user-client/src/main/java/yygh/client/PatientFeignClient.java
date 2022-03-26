package yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import yygh.model.user.Patient;

@FeignClient(value = "service-user",path = "/api/user/patient/inner")
@Repository
public interface PatientFeignClient {

    //获取就诊人信息，用于订单服务
    @GetMapping("/get/{id}")
    Patient getPatientOrder(@PathVariable Long id);
}
