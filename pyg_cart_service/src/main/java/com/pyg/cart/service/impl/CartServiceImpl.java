package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.mapper.TbSellerMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 这是添加购物车商品的方法
     *
     * @param cartList list集合的购物车详情（商家id 名字 商品明细）
     * @param itemId   商品sku的id
     * @param num      商品的数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num) {
        // 1获取到商家的id
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        String sellerId = item.getSellerId();
        // 2循环遍历cartList判断商家是否存在 最好不采用循环，因为是判断商家是否存在可单独提取一个方法
        Cart cart = searchCart(sellerId, cartList);
        if (cart == null) {
            // 3如果不存在新增一个cart即商家
            cart = new Cart();
            // 获取商家的名称
            cart.setSellerName(item.getSeller());
            cart.setSellerId(sellerId);
            // 这是将新的商品添加到orderItemList里去
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(itemId, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        } else {
            // 4如果存在找到对应商家是否存在该商品明细 存在
            TbOrderItem orderItem = searchOrderItem(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                // 5没有对应的商品，即增加商品
                orderItem = createOrderItem(itemId, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                // 6如果有对应的商品，增加数量，修改金额即可
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
                // 如果数量小于零移除商品
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                // 如果商家orderList为空则清空商家
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 查找oderitemList中书否有相同的oderitem
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItem(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 这是商家不存在创建新的orderitem
     *
     * @param itemId
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(Long itemId, int num) {
        TbOrderItem orderItem = new TbOrderItem();
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        orderItem.setNum(num);
        orderItem.setPrice(item.getPrice());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPicPath(item.getImage());
        orderItem.setTotalFee(new BigDecimal(num * item.getPrice().doubleValue()));
        return orderItem;
    }

    /**
     * 这是查询商家是否存在
     *
     * @param sellerId
     * @param cartList
     * @return
     */
    private Cart searchCart(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> searchByRedis(String name) {
        Object cartList = redisTemplate.boundHashOps("cartList").get(name);
        if (cartList == null) {
            return new ArrayList<>();
        }
        return (List<Cart>) cartList;
    }

    @Override
    public void addCartListInRedis(List<Cart> cartList, String name) {
        // 添加到redis中
        redisTemplate.boundHashOps("cartList").put(name, cartList);
    }

    /**
     * 合并购物车  这里太巧妙了
     *
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
        // 怎么合并呢跟上面的类似
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList_redis = addGoodsToCartList(cartList_redis, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList_redis;
    }
}
