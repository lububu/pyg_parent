package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import com.pyg.utils.CookieUtil;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    @RequestMapping("/login")
    public Map login(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName",name);
        return map;
    }

    /**
     * 这是查询购物车的信息
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        // 获取登录名判断是否登陆
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        // 如果未登录从cookie中取，登陆了从redis中取
        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (StringUtils.isEmpty(cartList)) {
            // cookie中的cartlist为空
            cartList = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList, Cart.class);
        if ("anonymousUser".equals(name)) {
            // 通过工具类从cookie中获取到cartList
            return cartList_cookie;
        } else {
            // 从redis中取
            List<Cart> cartList_redis = cartService.searchByRedis(name);
            // 判断cookie中是否有数据，有的话和redis进行合并
            if (cartList_cookie.size() > 0) {
                // 合并
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
            }
            // 清除cookie缓存
            CookieUtil.deleteCookie(request, response, "cartList");
            // 将合并后的存储到缓存中
            cartService.addCartListInRedis(cartList_redis, name);
            return cartList_redis;
        }
    }

    /**
     * 这是将商品添加到购物车
     *
     * @param itemId sku的id
     * @param num    商品的数量
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, int num) {
        // 允许跨域访问
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        // 由于下面用到了cookie所以允许携带cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        // 需要先查找cookie中的商品详情,然后在service层中处理
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            // 如果未登录添加到cookie中
            if ("anonymousUser".equals(name)) {
                // 将添加后的cartList存储到cookie中
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                // 添加到redis中
                cartService.addCartListInRedis(cartList, name);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }
}
