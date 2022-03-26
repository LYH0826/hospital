package yygh.controller.api;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.api.scripting.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yygh.model.user.UserInfo;
import yygh.result.Result;
import yygh.service.UserInfoService;
import yygh.utils.AuthContextHolder;
import yygh.utils.ConstantWxPropertiesUtil;
import yygh.utils.HttpClientUtils;
import yygh.vo.user.LoginVo;
import yygh.vo.user.UserAuthVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "用户管理：UserInfoApiController")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        //loginVo.setIp(IpUtil.getIpAddr(request));
        Map<String, Object> map = userInfoService.login(loginVo);
        return Result.ok(map);
    }

    @ApiOperation(value = "用户认证接口")
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    @ApiOperation(value = "获取用户id信息接口")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}
