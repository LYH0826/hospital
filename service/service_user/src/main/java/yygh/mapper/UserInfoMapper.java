package yygh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yygh.model.user.UserInfo;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
