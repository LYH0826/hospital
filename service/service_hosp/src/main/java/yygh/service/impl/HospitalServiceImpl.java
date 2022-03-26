package yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import yygh.client.DictFeignClient;
import yygh.exception.YyghException;
import yygh.model.hosp.Hospital;
import yygh.repository.HospitalRepository;
import yygh.result.Result;
import yygh.result.ResultCodeEnum;
import yygh.service.DepartmentService;
import yygh.service.HospitalService;
import yygh.vo.hosp.HospitalQueryVo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void saveHospital(Map<String, Object> map) {
        //把map集合转化成对象 Hospital
        //先转成 Map -> 字符串
        String mapStr = JSONObject.toJSONString(map);
        //将字符串 -> 对象
        Hospital hospital = JSONObject.parseObject(mapStr, Hospital.class);

        //获取医院密钥
        String hoscode = hospital.getHoscode();
        //返回的对象将用来判断数据库中是否存在该数据
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);

        //如果存在，则做修改操作
        if (hospitalByHoscode != null) {
            hospital.setStatus(hospitalByHoscode.getStatus());
            hospital.setCreateTime(hospitalByHoscode.getCreateTime());
            hospital.setUpdateTime(hospitalByHoscode.getUpdateTime());
            hospital.setIsDeleted(0);
            //执行修改操作
            hospitalRepository.save(hospital);
        } else {//如果不存在，则做添加操作
            //设置状态 状态 0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //修改操作
            hospitalRepository.save(hospital);
        }

    }

    //根据hoscode，获取hospital对象
    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    //分页查询
    @Override
    public Page<Hospital> getPage
    (Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        ExampleMatcher matching = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        Example<Hospital> example = Example.of(hospital, matching);
        Page<Hospital> hospitals = hospitalRepository.findAll(example, pageable);
        //获取Hospital的List集合并进行遍历，依次调用方法封装对应信息
        List<Hospital> hospitalsContent = hospitals.getContent();

        for (Hospital hospitalByParam : hospitalsContent) {
            this.setHospitalParam(hospitalByParam);
        }

        return hospitals;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(status == 0 || status == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }else {
            //status的值只能是1或0
            throw new YyghException(ResultCodeEnum.FAIL);
        }
    }

    @Override
    public Map<String, Object> getById(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        //将各种详情信息封装到该对象中
        this.setHospitalParam(hospital);
        //以Map方式返回更方便使用，当然也可以直接返回hostipal对象
        HashMap<String, Object> map = new HashMap<>();

        //将其中的bookingRule（预约规则）单独封装，方便后续使用
        map.put("bookingRule", hospital.getBookingRule());

        //无需重复返回
        hospital.setBookingRule(null);
        map.put("hospital", hospital);
        return map;
    }

    @Override
    public List<Hospital> findByHosName(String hospName) {
        return hospitalRepository.findHospitalByHosnameLike(hospName);
    }

    @Override
    public Map<String, Object> item(String hoscode) {
        HashMap<String, Object> result = new HashMap<>();
        //医院详情
        Hospital hospital = this.getByHoscode(hoscode);
        this.setHospitalParam(hospital);
        //预约规则
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复提交
        hospital.setBookingRule(null);
        result.put("hospital", hospital);
        return result;
    }

    //通过OpenFeign,将医院等级、省市地区的信息封装到查询要返回到Hospital对象中
    private void setHospitalParam(Hospital hospital) {
        //根据dict_code为Hostype的对象的ID(父ID)和value(医院类型)获取医院等级名称
        String hostypeName = dictFeignClient.getName("Hostype", hospital.getHostype());
        //查询相关的省市区，虽然现在数据中只有福建省和莆田市
        String provinceName = dictFeignClient.getName(hospital.getProvinceCode());
        String cityName = dictFeignClient.getName(hospital.getCityCode());
        String districtName = dictFeignClient.getName(hospital.getDistrictCode());


        //将查询到的数据封装到Hospital对象的param属性中
        hospital.getParam().put("provinceName", provinceName);
        hospital.getParam().put("cityName", cityName);
        hospital.getParam().put("districtName", districtName);
        hospital.getParam().put("hostypeName", hostypeName);
        //完整地址
        hospital.getParam().put("fullAddress", provinceName + cityName + districtName);

    }
}
