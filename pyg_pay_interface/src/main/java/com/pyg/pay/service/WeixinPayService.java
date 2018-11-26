package com.pyg.pay.service;

import java.util.Map;

public interface WeixinPayService {
    public Map createNative(String out_trade_no,String total_fee);

    Map queryPayStatus(String out_trade_no);

    Map closeOrder(String out_trade_no);
}
