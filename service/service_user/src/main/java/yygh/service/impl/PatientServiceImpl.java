package yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yygh.client.DictFeignClient;
import yygh.enums.DictEnum;
import yygh.mapper.PatientMapper;
import yygh.model.user.Patient;
import yygh.service.PatientService;

import java.util.List;

@Service
public class PatientServiceImpl
        extends ServiceImpl<PatientMapper, Patient>
        implements PatientService {
    @Autowired
    private DictFeignClient dictFeignClient;

    //获取就诊人列表
    @Override
    public List<Patient> findAllUserId(Long userId) {
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        //根据userId查询该用户下所有就诊人信息
        wrapper.eq("user_id", userId);
        List<Patient> patientList = baseMapper.selectList(wrapper);
        //通过远程调用，得到编码对应具体内容，查询数据字典表内容
        patientList.stream().forEach(patient -> {
            //为列表中的每一个Patient对象封装其他参数
            this.packPatient(patient);
        });
        return patientList;
    }

    //根据id获取就诊人信息
    @Override
    public Patient getPatientId(Long id) {
        Patient patient = baseMapper.selectById(id);
        this.packPatient(patient);
        return patient;
    }

    //通过查询数据字典来为Patient对象封装其他参数
    private void packPatient(Patient patient) {
        //根据证件类型编码，获取证件类型名称
        String certificatesTypeString =
                dictFeignClient.getName(
                        DictEnum.CERTIFICATES_TYPE.getDictCode(),
                        patient.getCertificatesType()
                );
        //获取对应联系人的证件类型
        String contactsCertificatesTypeString =
                dictFeignClient.getName(
                        DictEnum.CERTIFICATES_TYPE.getDictCode(),
                        patient.getContactsCertificatesType()
                );
        //省（目前只有福建省）
        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        //市（目前只有莆田市）
        String cityString = dictFeignClient.getName(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getName(patient.getDistrictCode());

        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put(
                "contactsCertificatesTypeString",
                contactsCertificatesTypeString
        );
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        //完整地址
        patient.getParam().put(
                "fullAddress",
                provinceString + cityString + districtString + patient.getAddress()
        );
    }
}
