package mytest;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

//测试EasyExcel
public class TestWrite {
    public static void main(String[] args) {
        //构建list数据集合
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUid(i);
            user.setName("lucy" + i);
            list.add(user);
        }
        //设置excel的文件路径和名称
        String fileName = "/Users/linyiheng/opt/mytest/ezexTest.xlsx";

        //调用方法实现写操作
        EasyExcel.write(fileName, User.class).sheet("用户信息")
                .doWrite(list);

    }
}
