package yygh.service;

import org.springframework.data.domain.Page;
import yygh.model.hosp.Hospital;
import yygh.vo.hosp.HospitalQueryVo;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    //上传医院接口，参数使用Map，减少对象封装，有利于签名校验
    void saveHospital(Map<String, Object> map);

    //查询医院
    Hospital getByHoscode(String hoscode);

    //多条件分页查询
    Page<Hospital> getPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //更新上线状态
    void updateStatus(String id,Integer status);

    //根据id获取医院数据---用于展示信息
    Map<String,Object> getById(String id);

    //根据医院名称获取数据 模糊查询
    List<Hospital> findByHosName(String hospName);

    //医院预约挂号详情
    Map<String,Object> item(String hoscode);

}
