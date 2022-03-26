package yygh.service;

import org.springframework.data.domain.Page;
import yygh.model.hosp.Department;
import yygh.vo.hosp.DepartmentQueryVo;
import yygh.vo.hosp.DepartmentVo;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //上传科室
    void saveDepartment(Map<String,Object> parameterMap);

    //查询科室：一个医院有多个科室，采取分页查询方式
    Page<Department> getPage(int pageNum, int pageSize, DepartmentQueryVo departmentQueryVo);

    //删除科室
    void delete(String hoscode,String depcode);

    //根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);

    //根据医院编号、科室编号，获取科室名称
    String getDeptName(String hoscode, String depcode);

    //根据医院编号、科室编号
    Department getDepartment(String hoscode, String depcode);
}
