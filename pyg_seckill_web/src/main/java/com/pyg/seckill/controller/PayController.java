package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
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
    private SeckillOrderService seckillOrderService;
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
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //判断支付日志存在
        if(seckillOrder!=null){
            String outTradeNo = seckillOrder.getId() + "";
            long totalFee=  (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
            return weixinPayService.createNative(outTradeNo,totalFee + "");
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
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
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
//                orderService.updateOrderStatus(out_trade_no,(String) result.get("transaction_id"));
                // 支付成功保存到数据库，补全数据，清除redis
                seckillOrderService.saveOrderFromRedisToDb(userId, (String) result.get("transaction_id"));
                return new Result(true, "支付成功");
            }
            try {
                // 每隔三秒查询一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if (count >= 5) {
                // 支付超时 执行关闭订单
                Map resultMap = weixinPayService.closeOrder(out_trade_no);
                // 判断通信是否成功
                if (!"SUCCESS".equals(resultMap.get("return_code"))) {
                    //通信不成功，判断是否支付成功
                    if ("ORDERPAID".equals(resultMap.get("err_code"))) {
                        // 成功返回结果
                        seckillOrderService.saveOrderFromRedisToDb(userId, (String) result.get("transaction_id"));
                        return new Result(true, "支付成功");
                    }
                }else {
                    // 关闭成功支付失败 删除订单
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                return new Result(false, "outTime");
            }
        }

    }
}
