package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 这是添加购物车商品的方法
     * @param cartList list集合的购物车详情（商家id 名字 商品明细）
     * @param itemId 商品sku的id
     * @param num   商品的数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,int num);

    List<Cart> searchByRedis(String name);

    void addCartListInRedis(List<Cart> cartList, String name);

    List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie);
}
