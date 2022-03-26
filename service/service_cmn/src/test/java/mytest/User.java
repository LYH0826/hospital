package mytest;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class User {
    //设置表头内容
    @ExcelProperty("用户编号")
    private int uid;

    @ExcelProperty("用户名称")
    private String name;
}
