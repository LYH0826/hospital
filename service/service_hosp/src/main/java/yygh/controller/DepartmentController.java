package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yygh.result.Result;
import yygh.service.DepartmentService;
import yygh.vo.hosp.DepartmentVo;

import java.util.List;

@Api(tags = "科室管理：DepartmentController")
@RestController
//@CrossOrigin
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("根据医院编号，查询医院所有科室列表")
    @GetMapping("/getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> deptTree = departmentService.findDeptTree(hoscode);
        return Result.ok(deptTree);
    }
}
