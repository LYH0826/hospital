package yygh.controller.api;

//import com.sun.deploy.net.URLEncoder;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import yygh.helper.JwtHelper;
import yygh.model.user.UserInfo;
import yygh.result.Result;
import yygh.service.UserInfoService;
import yygh.utils.ConstantWxPropertiesUtil;
import yygh.utils.HttpClientUtils;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "微信登录接口：WeixinApiController")
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("返回微信登录参数，以便生成微信二维码")
    @GetMapping("getLoginParam")
    @ResponseBody //以JSON字符串形式返回
    public Result genQrConnect(HttpSession session) {
        try {
            //根据微信APi接口要求，将回调地址进行UrlEncode
            String redirectUrl =
                    URLEncoder.encode(ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8");
            HashMap<String, Object> map = new HashMap<>();
            map.put("appid", ConstantWxPropertiesUtil.WX_OPEN_APP_ID);
            map.put("redirectUrl", redirectUrl);
            map.put("scope", "snsapi_login");
            map.put("state", System.currentTimeMillis() + "");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    //微信扫描后回调的方法，按照官方文档进行编写
    @GetMapping("callback")
    public String callback(String code, String state) {
        //扫码成功后通过微信官方平台返回临时票据code
        System.out.println("code = " + code);
        /*
        根据code和微信id和秘钥，请求微信固定地址，
        即使用code和appid以及appscrect换取access_token
        其中，%s为占位符
         */
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        //对占位符进行填充
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        //使用httpclient请求该地址
        try {
            //GET方式提交
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accessTokenInfo = " + accessTokenInfo);
            //从返回的字符串信息中获取openid 和 access_token
            //通过将字符串转换成JSON的方式获取，更为方便
            JSONObject jsonObject = JSONObject.parseObject(accessTokenInfo);
            String access_token = jsonObject.getString("access_token");
            //openid是微信官方给每个用户设置的唯一值，后续可以用作判断
            String openid = jsonObject.getString("openid");

            //根据openid判断数据库是否存在微信的扫描人信息
            UserInfo userInfo = userInfoService.selectWxInfoOpenId(openid);
            //若数据库中不存在该信息
            if (userInfo == null) {
                //再根据 openid 和 access_token请求微信地址，得到扫描人信息
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                //填充占位符
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                //通过httpclient发起get请求获得数据
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultInfo = " + resultInfo);
                //将返回的数据转换成JSON，获取所需用户信息
                JSONObject resultJson = JSONObject.parseObject(resultInfo);
                //获取昵称
                String nickname = resultJson.getString("nickname");
                //获取头像地址
                String headimgurl = resultJson.getString("headimgurl");

                //将该扫描人信息填入数据库
                userInfo = new UserInfo();
                userInfo.setStatus(1);
                userInfo.setAuthStatus(0);
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfoService.save(userInfo);
            }

            //返回name和token字符串，与UserInfoServiceImpl中的login方法相似
            Map<String, String> map = new HashMap<>();
            String name = userInfo.getName();
            //若有姓名并且通过认证，则把姓名显示
            if (userInfo.getAuthStatus() == 2 && !StringUtils.isEmpty(name)){
                map.put("name", name);
            }else {
                //若姓名为空或未通过认证，则把昵称当姓名显示
                name = userInfo.getNickName();
                map.put("name", name);
            }


            //判断userInfo是否有手机号，如果手机号为空，返回openid
            //如果手机号不为空，返回openid值是空字符串
            //前端判断：如果openid不为getPhone，绑定手机号，如果openid为getPhone，不需要绑定手机号
            if (StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid", userInfo.getOpenid());
            } else {
                userInfo.setOpenid("getPhone");
                map.put("openid", "getPhone");
            }

            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            //跳转到前端页面
            return "redirect:" +
                    ConstantWxPropertiesUtil.YYGH_BASE_URL +
                    "/weixin/callback?token=" + map.get("token") +
                    "&openid=" + map.get("openid") +
                    "&name=" + URLEncoder.encode(map.get("name"), "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
