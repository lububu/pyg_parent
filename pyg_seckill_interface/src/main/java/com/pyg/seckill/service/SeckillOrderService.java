package com.pyg.seckill.service;
import java.util.List;
import com.pyg.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	void submitOrder(Long seckill_id,String user_id);

	/**
	 * 根据用户id查询订单
	 * @param userId
	 * @return
	 */
	 TbSeckillOrder searchOrderFromRedisByUserId(String userId);

	void saveOrderFromRedisToDb(String userId, String transaction_id);

	/**
	 * 支付失败删除redis中的订单
	 * @param userId
	 * @param out_trade_no
	 */
	void deleteOrderFromRedis(String userId, Long out_trade_no);
}

