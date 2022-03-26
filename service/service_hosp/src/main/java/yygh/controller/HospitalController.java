package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import yygh.model.hosp.Hospital;
import yygh.result.Result;
import yygh.service.HospitalService;
import yygh.vo.hosp.HospitalQueryVo;

import java.util.Map;

@Api(tags = "医院管理：HospitalController")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @ApiOperation("多条件分页查询")
    @GetMapping("/pageList/{page}/{limit}")
    public Result pageList(@PathVariable("page") Integer page,
                           @PathVariable("limit") Integer limit,
                           HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalPage = hospitalService.getPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }

    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result lock(@PathVariable("id") String id,
                       @ApiParam(name = "status",
                               value = "状态（0：未上线 1：已上线）",
                               required = true)
                       @PathVariable("status") Integer status)
    {
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    //前端展示详情
    @ApiOperation(value = "根据医院id获取医院数据")
    @GetMapping("/getById/{id}")
    public Result getById(@PathVariable String id){
        Map<String, Object> map = hospitalService.getById(id);
        return Result.ok(map);
    }

}
