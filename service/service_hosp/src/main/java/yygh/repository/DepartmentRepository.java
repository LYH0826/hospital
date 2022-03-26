package yygh.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import yygh.model.hosp.Department;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //根据hoscode,depcode，查询数据库对应数据
    //命名规则需符合SpringData规范
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
