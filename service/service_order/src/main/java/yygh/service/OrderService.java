package yygh.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import yygh.model.order.OrderInfo;
import yygh.vo.order.OrderQueryVo;

public interface OrderService extends IService<OrderInfo> {

    //保存订单
    Long saveOrder(String scheduleId, Long patientId);

    //订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    //根据订单id查询订单详情
    OrderInfo getOrder(String orderId);
}
