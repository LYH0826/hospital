package yygh.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.StringUtils;
import yygh.exception.YyghException;
import yygh.result.ResultCodeEnum;
import yygh.service.HospitalSetService;

import java.util.Date;
import java.util.Map;

public class HospUtils {

    //对参数和签名进行校验
    public static void check(Map<String, Object> map, HospitalSetService hospitalSetService) {
        //参数校验
        String hoscode = (String) map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //根据传递过来的医院编码，查询数据库对应的签名，并进行加密
        //获取签名的过程会校验医院设置信息状态是否为可用
        String signKey = hospitalSetService.getSignKey(hoscode);
        String encryptKey = MD5.encrypt(signKey);
        //获取医院系统传来的签名，该签名进行MD5加密
        String hospSign = (String) map.get("sign");

        //判断签名是否一致
        if (!hospSign.equals(encryptKey)) {
            //不一致则报错：签名异常
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }

    //根据日期获取星期
    public static String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }


    //将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
    public static DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date)
                .toString("yyyy-MM-dd") + " " + timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }
}
