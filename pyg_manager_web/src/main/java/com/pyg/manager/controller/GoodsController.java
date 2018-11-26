package com.pyg.manager.controller;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbGoods;
import com.pyg.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	@Autowired
	private JmsTemplate jmsTemplate;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);
	}
	@Autowired
	private Destination queueSolrDestination;
	//这是主题模式
	@Autowired
	private Destination topicPageDestination;
	@RequestMapping("updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids,status);
			if ("1".equals(status)){
				List<TbItem> items = goodsService.updateItems(ids, status);
				for (TbItem item : items) {
					//获取到spe中的数据设置到specMap中
					Map map = JSON.parseObject(item.getSpec(), Map.class);
					item.setSpecMap(map);
				}
				//这里不再直接调用而通过中间件实现
//				itemSearchService.importItemData(items);
				jmsTemplate.send(queueSolrDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						//需要将这个item序列化  上传到消息中间件中
						String jsonString = JSON.toJSONString(items);
						return session.createTextMessage(jsonString);
					}
				});
//					itemPageService.genItemHtml(id);   //这里不再根据manage生成静态页面  将其传到 消息中间件中
//				jmsTemplate.send(topicPageDestination, new MessageCreator() {
//					@Override
//					public Message createMessage(Session session) throws JMSException {
//						return session.createObjectMessage(ids);
//					}
//				});
				for (Long id : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(id);
						}
					});
				}

			}
			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}

	@Autowired
	private Destination queueSolrDeleteDestination;
	@Autowired
	private Destination topicPageDeleteDestination;
	@RequestMapping("isDelete")
	public Result isDelete(Long[] ids) {
		try {
			goodsService.isDelete(ids);
			//这里不直接删除 通过中间件删除
//			itemSearchService.deleItems(ids);
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					//删除发的是一个对象过去
					return session.createObjectMessage(ids);
				}
			});
			//删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}

//	@RequestMapping("genItemHtml")
//	public Result genItemHtml(Long goodsId){
//		try {
//			itemPageService.genItemHtml(goodsId);
//			return new Result(true,"生成成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Result(false,"生成失败");
//		}
//	}
}
