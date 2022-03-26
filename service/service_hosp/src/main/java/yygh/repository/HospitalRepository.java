package yygh.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import yygh.model.hosp.Hospital;

import java.util.List;

/*
    Spring Data提供了对mongodb数据访问的支持
    只需要继承MongoRepository类，按照Spring Data规范，SpringData会自动根据类名来实现方法
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    // 判断是否存在数据
    Hospital getHospitalByHoscode(String hoscode);

    //根据医院名称获取数据 模糊查询
    List<Hospital> findHospitalByHosnameLike(String hospName);
}
