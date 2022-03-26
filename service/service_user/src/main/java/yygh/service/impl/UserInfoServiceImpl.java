package yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import yygh.enums.AuthStatusEnum;
import yygh.exception.YyghException;
import yygh.helper.JwtHelper;
import yygh.mapper.UserInfoMapper;
import yygh.model.user.Patient;
import yygh.model.user.UserInfo;
import yygh.result.ResultCodeEnum;
import yygh.service.PatientService;
import yygh.service.UserInfoService;
import yygh.vo.user.LoginVo;
import yygh.vo.user.UserAuthVo;
import yygh.vo.user.UserInfoQueryVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl
        extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //用户登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //获取登录手机号与验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //校验参数是否为空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //校验验证码
        String mobileCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(mobileCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //绑定手机号码
        UserInfo userInfo = null;
        UserInfo wxuserInfo = null;
        //有openid说明是有过微信登录，一旦有微信登录则一定会绑定手机
        if (!loginVo.getOpenid().equals("")) {
            wxuserInfo = this.selectWxInfoOpenId(loginVo.getOpenid());
            if (null != wxuserInfo) {
                //判断数据库中是否有相同手机号的数据
                QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("phone", phone);
                userInfo = baseMapper.selectOne(wrapper);
                //存在相同手机号用户，则将微信数据绑定到该用户上
                if (null != userInfo) {
                    userInfo.setOpenid(wxuserInfo.getOpenid());
                    userInfo.setNickName(wxuserInfo.getNickName());
                    this.updateById(userInfo);
                    //没有存在相同手机号，自己绑定手机
                } else {
                    wxuserInfo.setPhone(loginVo.getPhone());
                    this.updateById(wxuserInfo);
                    userInfo = new UserInfo();
                    BeanUtils.copyProperties(wxuserInfo, userInfo);
                }
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //如果userinfo为空，进行正常手机登录
        if (userInfo == null) {
            //判断是否为第一次登录，即根据手机号查询数据库，若不存在相同手机号便是第一次登录
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(wrapper);

            //第一次使用该手机号登录
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setStatus(1);//设置状态为可用
                userInfo.setName("");
                userInfo.setAuthStatus(0);
                userInfo.setPhone(phone);
                baseMapper.insert(userInfo);
            }
        }

        //校验是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //若已经注册过，则直接登录，并返回相应的登录信息
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();

        //若有姓名并且通过认证，则把姓名显示
        if (userInfo.getAuthStatus() == 2 && !StringUtils.isEmpty(name)) {
            map.put("name", name);
        } else {
            //若姓名为空或未通过认证，则把昵称当姓名显示
            name = userInfo.getNickName();
            map.put("name", name);
        }

        //若还为空，则把手机号作为名称返回
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
            map.put("name", name);
        }

        //逻辑删除掉所有手机号码为空的无用数据
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", "");
        this.remove(wrapper);

        //jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    //根据微信独有的openid查找数据
    @Override
    public UserInfo selectWxInfoOpenId(String openid) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        return baseMapper.selectOne(wrapper);
    }

    //用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //证件类型
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        //证件号码
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        //上传图片地址
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        //认证状态---认证中
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());

        //更新数据库中的信息
        baseMapper.updateById(userInfo);
    }

    //用户列表（条件查询带分页）
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间

        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            //模糊查询
            wrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            //大于等于开始时间
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            //小于等于结束时间
            wrapper.le("create_time", createTimeEnd);
        }

        //调用mapper的方法，查询数据库
        IPage<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return pages;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if (status == 0 || status == 1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        //根据userId查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //封装参数
        this.packageUserInfo(userInfo);
        //根据userId查询用户的所有就诊人信息
        List<Patient> patientList = patientService.findAllUserId(userId);
        //将数据封装到map集合中并返回
        map.put("userInfo", userInfo);
        map.put("patientList", patientList);
        return map;
    }

    //认证审批
    @Override
    public void approval(Long userId, Integer authStatus) {
        //设置为通过或者不通过
        if (authStatus == 2 || authStatus == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //将编号转换为对应值进行封装
    private void packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态编码
        String statusString = userInfo.getStatus() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
    }
}
