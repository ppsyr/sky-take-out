package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("处理超时订单");
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Orders> list = orderMapper.getByTime(Orders.PENDING_PAYMENT, time);
        if(list != null && !list.isEmpty()){
            list.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("支付超时");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("处理派送异常订单");
        LocalDateTime time = LocalDateTime.now().minusHours(1);

        List<Orders> list = orderMapper.getByTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (list != null && !list.isEmpty()) {
            list.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                orderMapper.update(order);
            });
        }
    }
}
