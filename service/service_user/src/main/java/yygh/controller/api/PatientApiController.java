package yygh.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yygh.model.user.Patient;
import yygh.result.Result;
import yygh.service.PatientService;
import yygh.utils.AuthContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "就诊人管理接口：PatientApiController")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {
    @Autowired
    private PatientService patientService;

    @ApiOperation("获取就诊人列表")
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request) {
        //获取当前登录用户的ID
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patientList = patientService.findAllUserId(userId);
        return Result.ok(patientList);
    }

    @ApiOperation("添加就诊人")
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户的ID
        Long userId = AuthContextHolder.getUserId(request);
        //给要添加的就诊人绑定当前用户ID
        patient.setUserId(userId);
        patient.setProvinceCode("86");
        patient.setCityCode("110000");
        patient.setDistrictCode("100101");
        patientService.save(patient);
        return Result.ok();
    }

    @ApiOperation("根据用户id获取就诊人信息")
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable("id") Long id) {
        Patient patient = patientService.getPatientId(id);
        return Result.ok(patient);
    }

    @ApiOperation("根据就诊人id获取就诊人信息，用于订单服务")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id) {
        return patientService.getPatientId(id);
    }

    @ApiOperation("修改就诊人")
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    @ApiOperation("删除就诊人")
    @DeleteMapping("auth/remove/{id}")
    public Result removePatient(@PathVariable("id") Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

}
