package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.utils.IdWorker;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Autowired
    private IdWorker idWorker;

    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }



    /**
     * 查询订单的状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        // 死循环一直查询是否支付成功
        int count = 0; // 定义一个计数器
        while (true) {
            // 调用service判断是否支付成功
            Map result = weixinPayService.queryPayStatus(out_trade_no);
            // 判断结果
            if (result == null) {
                return new Result(false, "支付失败");
            }
            // 如果返回成功
            if ("SUCCESS".equals(result.get("trade_state"))) {
                // 支付成功修改订单状态
                orderService.updateOrderStatus(out_trade_no,(String) result.get("transaction_id"));
                return new Result(true, "支付成功");
            }
            try {
                // 每隔三秒查询一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if (count >= 100) {
                return new Result(false, "outTime");
            }
        }

    }
}
