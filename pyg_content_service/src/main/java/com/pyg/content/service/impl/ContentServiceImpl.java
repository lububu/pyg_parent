package com.pyg.content.service.impl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pyg.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbContentMapper;
import com.pyg.pojo.TbContent;
import com.pyg.pojo.TbContentExample;
import com.pyg.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//增辖修改了数据库需要重新加载缓存  只需要把修改的那个类型的广告删除即可
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		contentMapper.insert(content);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//修改可能会影响两个redis  一个会增加一个可能会减少  删除的时候两个redis都需要删除
//		现根据其id查找到以前的categoryId
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		//然后将影响的redis删除
		redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		contentMapper.updateByPrimaryKey(content);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		HashSet<Long> set = new HashSet<>();
		for(Long id:ids){
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			set.add(tbContent.getCategoryId());
			contentMapper.deleteByPrimaryKey(id);
		}
		for (Long categoryId : set) {
			redisTemplate.boundHashOps("content").delete(categoryId);
		}
	}


	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();

		if(content!=null){
			if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}

		}

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
//		现在问题是怎么添加到redis中  怎么添加到redis中
		//这里是根据广告类型查询
		List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		if (contents!=null&&contents.size()>0) {
			System.out.println("从缓存中查询");
		}else {
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			//查询的结果可以进行筛选 其状态必须是一
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("sort_order");
			contents = contentMapper.selectByExample(example);
			//从数据库中查询到结果 添加到redis中  根据其外键添加
			redisTemplate.boundHashOps("content").put(categoryId, contents);
			System.out.println("从数据库查询");
		}
		return contents;
	}

}
