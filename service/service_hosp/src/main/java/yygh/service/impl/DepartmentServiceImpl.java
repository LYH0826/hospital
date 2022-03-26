package yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import yygh.model.hosp.Department;
import yygh.repository.DepartmentRepository;
import yygh.service.DepartmentService;
import yygh.vo.hosp.DepartmentQueryVo;
import yygh.vo.hosp.DepartmentVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void saveDepartment(Map<String, Object> parameterMap) {
        //将map集合通过JSONObject转换为Department对象
        String jsonString = JSONObject.toJSONString(parameterMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);

        //使用该对象来判断数据库中是否已经存在该信息
        Department targetDepartment = departmentRepository
                .getDepartmentByHoscodeAndDepcode(
                        department.getHoscode(),
                        department.getDepcode()
                );

        //数据库存在该信息，做修改操作
        if (targetDepartment != null) {
            targetDepartment.setIsDeleted(0);
            targetDepartment.setUpdateTime(new Date());
            departmentRepository.save(targetDepartment);
        } else {
            //数据库不存在该信息，做添加操作
            department.setIsDeleted(0);
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            departmentRepository.save(department);
        }

    }

    //多条件科室的分页查询
    @Override
    public Page<Department> getPage
    (int pageNum, int pageSize, DepartmentQueryVo departmentQueryVo) {
        //构建排序规则
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //构建分页规则，其中0为第一页
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        //构建匹配规则: 模糊查询并忽略大小写
        ExampleMatcher matching = ExampleMatcher.matching();
        matching.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        //将条件查询类中的数据复制到实体类
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        Example<Department> example = Example.of(department, matching);

        return departmentRepository.findAll(example, pageable);
    }

    //删除科室
    @Override
    public void delete(String hoscode, String depcode) {
        Department department =
                departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            departmentRepository.delete(department);
        }

    }

    //根据医院编号，查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //用于最终返回的数据
        ArrayList<DepartmentVo> resultList = new ArrayList<>();

        //创建一个用于构建查询条件的类
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);
        //返回该医院下所有科室列表信息
        List<Department> departments = departmentRepository.findAll(example);

        //根据大科室编号(bigCode)进行分组，方便后续前端进行树形数据显示
        //使用java8的新特性将List集合打成流，然后根据大科室编号(bigCode)进行分组
        //此时key便是大科室编号，value便是对应编号下的所有子科室
        Map<String, List<Department>> map =
                departments.stream().collect(
                        //lambada表达式
                        Collectors.groupingBy(Department::getBigcode)
                );

        //遍历map集合,并对数据进行封装，方便前端调用
        for (Map.Entry<String, List<Department>> entry : map.entrySet()) {
            //大科室编号
            String bigCode = entry.getKey();
            //对应编号下的所有子科室
            List<Department> childDeptList = entry.getValue();

            //封装大科室,DepartmentVo更适合前端获取
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            bigDepartmentVo.setDepcode(bigCode);
            //get()里面的index可以任意，因为子科室内所有的大科室名称都一样
            bigDepartmentVo.setDepname(childDeptList.get(0).getDepname());

            //封装子科室信息
            ArrayList<DepartmentVo> childList = new ArrayList<>();
            for (Department childDept : childDeptList) {
                DepartmentVo childDepartmentVo = new DepartmentVo();
                childDepartmentVo.setDepname(childDept.getDepname());
                childDepartmentVo.setDepcode(childDept.getDepcode());
                childList.add(childDepartmentVo);
            }

            //把子科室的childList集合封装到大科室内
            bigDepartmentVo.setChildren(childList);

            //放入最终返回的集合中
            resultList.add(bigDepartmentVo);
        }
        return resultList;
    }

    //根据医院编号、科室编号，获取科室名称
    @Override
    public String getDeptName(String hoscode, String depcode) {
        Department department =
                departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null){
            return department.getDepname();
        }

        return null;
    }

    //根据医院编号、科室编号，获取科室
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
