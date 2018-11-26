package com.pyg.task.service;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TaskService {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 更新秒杀的商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void updateSecKillGoodsFromRedis() {
       // 查询数据库看是否有符合条件的商品
        List ids = new ArrayList( redisTemplate.boundHashOps("secKill").keys());
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);
        criteria.andStartTimeLessThan(new Date());
        criteria.andEndTimeGreaterThan(new Date());
        criteria.andIdNotIn(ids);//排除缓存中已经有的商品
        List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
        // 循环添加到redis中
       if (seckillGoods !=null && seckillGoods.size() > 0) {
           for (TbSeckillGoods seckillGood : seckillGoods) {
               redisTemplate.boundHashOps("secKill").put(seckillGood.getId(),seckillGood);
               System.out.println("更新了部分商品");
           }
       }
        System.out.println("正在查询更新1");
    }

    @Scheduled(cron = "* * * * * ?")
    public void deleSecKillGoodsOutTime() {
        // 获取缓存中的商品
        List<TbSeckillGoods> secKill = redisTemplate.boundHashOps("secKill").values();
        if (secKill != null && secKill.size() >0) {
            for (TbSeckillGoods seckillGoods : secKill) {
                if (seckillGoods.getEndTime().getTime() <= new Date().getTime()) {
                    // 更新数据库的数据
                    seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                    System.out.println("更新到了数据库");
                }
            }
        }
        System.out.println("正在查询更新2");
    }
}
