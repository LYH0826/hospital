package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yygh.model.hosp.Schedule;
import yygh.result.Result;
import yygh.service.ScheduleService;

import java.util.List;
import java.util.Map;

@Api(tags = "排班管理：ScheduleController")
@RestController
//@CrossOrigin
@RequestMapping("/admin/hosp/Schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("根据、医院编号、科室编号、分页、查询排班规则数据")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable("page") long page,
                                  @PathVariable("limit") long limit,
                                  @PathVariable("hoscode") String hoscode,
                                  @PathVariable("depcode") String depcode){

        Map<String,Object> map = scheduleService.getScheduleRulePage(page,limit,hoscode,depcode);

        return Result.ok(map);

    }

    @ApiOperation("根据医院编号、科室编号、工作日期，查询排班详细信息")
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable("hoscode") String hoscode,
                                    @PathVariable("depcode") String depcode,
                                    @PathVariable("workDate") String workDate){
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return Result.ok(list);
    }


}
