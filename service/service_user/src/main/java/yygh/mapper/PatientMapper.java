package yygh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yygh.model.user.Patient;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

}
