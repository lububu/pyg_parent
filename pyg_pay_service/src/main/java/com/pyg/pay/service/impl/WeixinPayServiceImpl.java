package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.utils.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 生成二维码
     *
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map<String, String> param = new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", out_trade_no);//商户订单号
        param.put("total_fee", total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://test.itcast.cn");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 查询订单的状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            // 封装请求
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);
            param.put("mch_id", partner);
            param.put("out_trade_no", out_trade_no);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            // 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");// 请求的url
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);// 这里面有签名
            httpClient.setXmlParam(xmlParam);
            httpClient.setHttps(true);
            httpClient.post();// 发送请求
            // 获得结果
            String content = httpClient.getContent();
            // 将结果转换成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            // 封装结果
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行关闭订单
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closeOrder(String out_trade_no) {
        try {
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            Map<String, String> param = new HashMap<>();
            // 准备需要的xml数据
            param.put("appid",appid);
            param.put("mch_id",partner);
            param.put("nonce_str",WXPayUtil.generateNonceStr());
            param.put("out_trade_no",out_trade_no);
            // 这里面有签名
            String xmlparam = WXPayUtil.generateSignedXml(param,partnerkey);
            httpClient.setXmlParam(xmlparam);
            httpClient.setHttps(true);
            // 发送请求
            httpClient.post();
            // 获取结果
            String content = httpClient.getContent();
            // 封装结果
            Map<String, String> map = WXPayUtil.xmlToMap(content);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
