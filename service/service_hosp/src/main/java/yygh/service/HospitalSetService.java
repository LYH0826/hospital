package yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import yygh.model.hosp.HospitalSet;
import yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {
    //获取签名key
    String getSignKey(String hoscode);

    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
