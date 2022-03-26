package yygh.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import yygh.exception.YyghException;
import yygh.model.hosp.HospitalSet;
import yygh.result.Result;
import yygh.service.HospitalSetService;
import yygh.utils.MD5;
import yygh.vo.hosp.HospitalSetQueryVo;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理：HospitalSetController")
//统一使用自定义的类Result返回数据，方便前端使用
@RestController
@RequestMapping("admin/hosp/hospitalSet")
//@CrossOrigin //允许跨域访问
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "查询医院设置表中的所有内容")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        //测试异常捕获
        //try {
        //    int a = 10/0;
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    throw new YyghException("", 201);
        //}
        List<HospitalSet> hospitalSets = hospitalSetService.list();
        return Result.ok(hospitalSets);
    }

    @ApiOperation(value = "根据id逻辑删除医院设置表中的对应信息")
    @ApiParam(name = "id", value = "医院设置表的id号", required = true)
    @DeleteMapping("{id}")
    public Result delHospitalSet(@PathVariable("id") Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (flag) {
            return Result.ok("删除id为" + id + " 的医院信息成功!");
        }
        return Result.fail("删除失败！id为" + id + "的医院信息不存在！");
    }

    //使用@RequestBody注解需要用POST方法传值
    @ApiOperation(value = "带分页带条件查询")
    @PostMapping("findHospitalSetByPage/{current}/{size}")
    public Result findHospitalSetByPage
    (@PathVariable("current") long current,//当前页
     @PathVariable("size") long size,//每页记录数
     //为方便前端操作，使用json形式传值
     //HospitalSetQueryVo对象中封装了医院编号和医院名称，方便条件查询使用
     @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        //创建page对象：传递当前页和每页记录数
        Page<HospitalSet> page = new Page<HospitalSet>(current, size);

        //使用Mybatis-plus提供的QueryWrapper对象：构造条件
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        String hoscode = hospitalSetQueryVo.getHoscode();//获取医院编号
        String hosname = hospitalSetQueryVo.getHosname();//获取医院名称

        //判断是否附带条件进行查询
        if (!StringUtils.isEmpty(hoscode)) {
            wrapper.eq("hoscode", hoscode);
        }
        if (!StringUtils.isEmpty(hosname)) {
            wrapper.like("hosname", hosname);
        }
        //调用Mybatis-plus中封装好的分页方法实现功能
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);

        return Result.ok(hospitalSetPage);

    }

    @ApiOperation(value = "添加医院设置信息")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        //设置状态，其中1表示可用，0表示不可用，每次新添加医院信息时设置为可用
        hospitalSet.setStatus(1);
        //设置签名密钥，后续与医院接口进行对接时使用，使用MD5加密
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));

        boolean flag = hospitalSetService.save(hospitalSet);
        if (flag) {
            return Result.ok("添加医院设置信息成功！");
        }
        return Result.fail("添加医院设置信息失败！");
    }

    @ApiOperation(value = "根据ID获取医院设置信息")
    @GetMapping("getHospitalSetByID/{id}")
    public Result getHospitalSetByID(@PathVariable("id") Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        System.out.println(hospitalSet);
        if (hospitalSet != null)
            return Result.ok(hospitalSet);
        return Result.fail("未查询到id: " + id + "所对应的医院信息");
    }

    @ApiOperation(value = "通过ID修改医院设置信息")
    @PostMapping("updateHospitalSetByID")
    public Result updateHospitalSetByID(@RequestBody HospitalSet hospitalSet) {
        hospitalSet.setUpdateTime(new Date());
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (flag) {
            return Result.ok("修改医院设置信息成功！");
        }
        return Result.fail("修改医院设置信息失败！");
    }

    @ApiOperation(value = "批量删除医院设置信息")
    @DeleteMapping("batchRemoveHospitalSet")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> ids) {
        boolean flag = hospitalSetService.removeByIds(ids);
        //只要能删除一个便是true，否则均为false
        if (flag) {
            return Result.ok("批量删除医院设置信息成功！");
        }
        return Result.fail("批量删除医院设置信息失败！所查询的信息均不存在！");
    }

    //只有医院系统解锁状态，才能和医院系统对接，实现数据操作，规定1为可用，0为不可用
    @ApiOperation(value = "通过ID修改医院状态")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable("id") Long id,
                                  @PathVariable("status") Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet == null) {
            return Result.fail("修改状态失败！未查询到相关医院信息");
        }
        if (!(status == 1 || status == 0)) {
            return Result.fail("输入的状态码只能是1或0！");
        }
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return Result.ok("修改id为 " + id + " 的医院状态成功！");
    }

    @ApiOperation(value = "通过ID发送签名密钥")
    @PutMapping("sendKey/{id}")
    public Result sendKey(@PathVariable("id") Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet != null) {
            String hoscode = hospitalSet.getHoscode();
            String signKey = hospitalSet.getSignKey();
        }
        return Result.ok("还在开发中...");
    }


}
