package yygh.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yygh.exception.YyghException;
import yygh.helper.HttpRequestHelper;
import yygh.model.hosp.Department;
import yygh.model.hosp.Hospital;
import yygh.model.hosp.Schedule;
import yygh.result.Result;
import yygh.result.ResultCodeEnum;
import yygh.service.DepartmentService;
import yygh.service.HospitalService;
import yygh.service.HospitalSetService;
import yygh.service.ScheduleService;
import yygh.utils.HospUtils;
import yygh.utils.MD5;
import yygh.vo.hosp.DepartmentQueryVo;
import yygh.vo.hosp.ScheduleQueryVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(value = "平台对外开发的接口都写在该Controller类")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    @ApiOperation("上传医院")
    @PostMapping("/saveHospital")
    //通过HttpServletRequest来获得从来的数据
    public Result saveHospital(HttpServletRequest request) {
        //获取传来的数据，并通过自定义的工具类转化一下泛型里面的类型，方便后续操作
        Map<String, String[]> parameterMap = request.getParameterMap();
        //将Map<String, String[]>转化为Map<String, Object>
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        //在传输过程中，图片路径中的"+"被换成了空格
        String logoData = (String) map.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        map.put("logoData", logoData);

        //调用Service的方法
        hospitalService.saveHospital(map);
        return Result.ok();
    }

    @ApiOperation("查询医院")
    @PostMapping("/hospital/show")
    public Result show(HttpServletRequest request) {
        //获取传来的数据，并通过自定义的工具类转化一下泛型里面的类型，方便后续操作
        Map<String, String[]> parameterMap = request.getParameterMap();
        //将Map<String, String[]>转化为Map<String, Object>
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        String hoscode = (String) map.get("hoscode");
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation("上传科室")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        departmentService.saveDepartment(map);
        return Result.ok();
    }


    @ApiOperation("获取分页列表")
    @PostMapping("/department/list")
    public Result list(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        //判断是否有值，如果没有则取默认值
        int pageNum =
                StringUtils.isEmpty(map.get("pageNum")) ?
                        1 : Integer.parseInt((String) map.get("pageNum"));
        int pageSize =
                StringUtils.isEmpty(map.get("pageSize")) ?
                        10 : Integer.parseInt((String) map.get("pageSize"));

        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        //构造科室条件查询对象
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);

        //多条件科室分页查询
        Page<Department> pageList =
                departmentService.getPage(pageNum, pageSize, departmentQueryVo);

        return Result.ok(pageList);
    }

    @ApiOperation("删除科室")
    @PostMapping("department/remove")
    public Result remove(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");

        departmentService.delete(hoscode, depcode);
        return Result.ok();
    }


    @ApiOperation("上传排班")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        scheduleService.save(map);
        return Result.ok();
    }

    @ApiOperation("分页查询排班")
    @PostMapping("/schedule/list")
    public Result listPageSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        //判断是否有值，如果没有则取默认值
        int pageNum =
                StringUtils.isEmpty(map.get("pageNum")) ?
                        1 : Integer.parseInt((String) map.get("pageNum"));
        int pageSize =
                StringUtils.isEmpty(map.get("pageSize")) ?
                        10 : Integer.parseInt((String) map.get("pageSize"));

        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        //构造排班条件查询对象
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        //多条件排班分页查询
        Page<Schedule> pageList =
                scheduleService.getPage(pageNum, pageSize, scheduleQueryVo);

        return Result.ok(pageList);
    }

    @ApiOperation("删除排班")
    @PostMapping("/schedule/remove")
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //校验
        HospUtils.check(map, hospitalSetService);

        String hoscode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");

        scheduleService.delete(hoscode, hosScheduleId);
        return Result.ok();
    }


}
