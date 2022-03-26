package yygh.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yygh.model.hosp.Hospital;
import yygh.result.Result;
import yygh.service.DepartmentService;
import yygh.service.HospitalService;
import yygh.service.HospitalSetService;
import yygh.service.ScheduleService;
import yygh.vo.hosp.HospitalQueryVo;
import yygh.vo.hosp.ScheduleOrderVo;
import yygh.vo.order.SignInfoVo;

import java.util.List;

@Api(tags = "医院接口：HospApiController")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("查询医院列表")
    @GetMapping("/getHospListPage/{page}/{limit}")
    public Result getHospListPage(@PathVariable("page") Integer page,
                                  @PathVariable("limit") Integer limit,
                                  HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalPage = hospitalService.getPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }

    @ApiOperation("根据医院名称获取数据 模糊查询")
    @GetMapping("/findByHospName/{hospName}")
    public Result findByHospName(@PathVariable("hospName") String hospName){
        List<Hospital> hospitalList = hospitalService.findByHosName(hospName);
        return Result.ok(hospitalList);
    }

    @ApiOperation("获取科室列表")
    @GetMapping("department/{hoscode}")
    public Result getDeptTree(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return Result.ok(departmentService.findDeptTree(hoscode));
    }

    @ApiOperation("医院预约挂号详情")
    @GetMapping("{hoscode}")
    public Result getHospInfo(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return Result.ok(hospitalService.item(hoscode));
    }

    @ApiOperation("获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable("page") Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable("limit") Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable("depcode") String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation("获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable("depcode") String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable("workDate") String workDate) {
        return Result.ok(scheduleService.getScheduleDetail(hoscode, depcode, workDate));
    }

    @ApiOperation("根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return Result.ok(scheduleService.getById(scheduleId));
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }
}

