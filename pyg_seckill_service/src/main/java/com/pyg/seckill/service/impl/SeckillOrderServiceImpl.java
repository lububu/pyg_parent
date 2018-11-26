package com.pyg.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pyg.pojo.TbSeckillGoods;
import com.pyg.seckill.service.SeckillOrderService;
import com.pyg.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}


	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}

		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckill_id, String user_id) {
		// 向redis中查询商品是否还存在
		TbSeckillGoods secKillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("secKill").get(seckill_id);
		// 商品不存在了
		if (secKillGoods == null) {
			throw new RuntimeException("商品不存在");
		}
		// 如果商品的数量小于0清楚缓存
		if (secKillGoods.getStockCount() <= 0) {
			redisTemplate.boundHashOps("secKill").delete(seckill_id);
			throw new RuntimeException("商品已抢购一空");

		}
		// redis中商品的库存数减1
		secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
		// 将其重新添加回redis
		redisTemplate.boundHashOps("secKill").put(seckill_id, secKillGoods);

		// 添加秒杀商品的订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setSeckillId(seckill_id);
		seckillOrder.setStatus("0");
		seckillOrder.setUserId(user_id);
		seckillOrder.setSellerId(secKillGoods.getSellerId());
		seckillOrder.setMoney(secKillGoods.getCostPrice());
		// 将订单存入到redis中
		redisTemplate.boundHashOps("secKillOrder").put(user_id, seckillOrder);
	}

	/**
	 * 根据用户id查询订单
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("secKillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, String transaction_id) {
		//根据用户ID查询日志
		TbSeckillOrder seckillOrder=(TbSeckillOrder)redisTemplate.boundHashOps("secKillOrder").get(userId);
		seckillOrder.setTransactionId(transaction_id);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("2");//状态
		seckillOrderMapper.insert(seckillOrder);//保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);//从redis中清除
	}

	/**
	 * 支付失败删除redis中的订单
	 *
	 * @param userId
	 * @param out_trade_no
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long out_trade_no) {
		// 根据userId 获取到redis中的订单
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
		// 查询到秒杀商品的id
		Long seckillId = seckillOrder.getSeckillId();
		// 查询redis中的秒杀商品,修改库存数量+1
		TbSeckillGoods secKill = (TbSeckillGoods) redisTemplate.boundHashOps("secKill").get(seckillId);
		System.out.println(secKill.getStockCount());
		secKill.setStockCount(secKill.getStockCount() + 1);
		System.out.println(secKill.getStockCount());
		redisTemplate.boundHashOps("secKill").put(seckillId,secKill);
		// 删除订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}
}
