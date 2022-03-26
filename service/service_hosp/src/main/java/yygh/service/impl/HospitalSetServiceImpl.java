package yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yygh.exception.YyghException;
import yygh.mapper.HospitalSetMapper;
import yygh.model.hosp.HospitalSet;
import yygh.result.ResultCodeEnum;
import yygh.service.HospitalSetService;
import yygh.vo.order.SignInfoVo;

@Service
public class HospitalSetServiceImpl
        extends ServiceImpl<HospitalSetMapper, HospitalSet>
        implements HospitalSetService {

    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Override
    public String getSignKey(String hoscode) {
        HospitalSet hospitalSet = this.getByHoscode(hoscode);
        if(null == hospitalSet) {
            //医院未开通，暂时不能访问
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        if(hospitalSet.getStatus() == 0) {
            //医院被锁定，暂时不能访问
            throw new YyghException(ResultCodeEnum.HOSPITAL_LOCK);
        }
        return hospitalSet.getSignKey();
    }

    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if (null == hospitalSet){
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }

    /**
     * 根据hoscode获取医院设置信息
     */
    private HospitalSet getByHoscode(String hoscode) {
        return hospitalSetMapper.selectOne(new QueryWrapper<HospitalSet>().eq("hoscode", hoscode));
    }

}
