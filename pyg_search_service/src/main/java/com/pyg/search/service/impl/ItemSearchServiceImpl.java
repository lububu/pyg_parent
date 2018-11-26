package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    //$scope.searchMap={keywords:'三星'}
    @Override
    public Map<String, Object> search(Map searchMap) {
        //这里先处理传过来的关键字可能为空字符串
        String keywords = (String) searchMap.get("keywords");
        keywords=keywords.replaceAll(" ","");
        searchMap.put("keywords",keywords);
        Map<String, Object> resultMap = new HashMap();
        //根据关键字高亮显示
        resultMap.putAll(searchHighLight(searchMap));
        //查询分类列表
        List<String> categoryList = searchCategory(searchMap);
        resultMap.put("categoryList", categoryList);
        //查询商品规格从redis缓存中进行查询
        //先判断查询的结果是否有分类
        if (categoryList.size() > 0) {
//            说明有分类从redis中提取数据   默认查询的是第一个   通过redis和typeId获取到所有规格和品牌集合
            //获取到模板id
            Map brandAndSpec = new HashMap();
            if (!"".equals(searchMap.get("category"))) {
                //这里是通过选择的分类查询
                brandAndSpec = searchBrandAndSpec((String) searchMap.get("category"));
            } else {//默认按第一个查询
                brandAndSpec = searchBrandAndSpec(categoryList.get(0));
            }
            resultMap.putAll(brandAndSpec);
        }
        return resultMap;//$scope.resultMa={total:100,itemList:[]}
    }


    public Map searchBrandAndSpec(String category) {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("categoryList").get(category);
        //根据模板id查 品牌和规格
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
//            将数据封装到map中传给前端
        map.put("brandList", brandList);
        map.put("specList", specList);
        return map;
    }

    private List searchCategory(Map searchMap) {
        //查询分类列表
        //封装查询的条件
        List list = new ArrayList();
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> result = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> entries = result.getGroupEntries();//获取到原型实体
        List<GroupEntry<TbItem>> content = entries.getContent();//这里是获得分组好的内容
        for (GroupEntry<TbItem> groupEntry : content) {
            list.add(groupEntry.getGroupValue());//这里是获取到里面的值并添加到集合中
        }
        return list;
    }

    //根据关键字进行高亮查询
    private Map searchHighLight(Map searchMap) {
        Map map = new HashMap();
        //封装查询的对象
        HighlightQuery query = new SimpleHighlightQuery();
        //这里是封装关键字  封装到条件里面
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //这里是对高亮的内容进行封装
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title");
        //还需要设置高亮的前缀 和后缀即样式
        options.setSimplePrefix("<em style='color:red'>");
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);
        //这是根据条件后查询到所有的结果
        //按分类查询
        if (!"".equals(searchMap.get("category")) && searchMap.get("category") != null) {
            //不等于空说明有分类通过分类去查询
            FilterQuery filterQuery = new SimpleFilterQuery();
            //这里必须重新new这个条件对象
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按品牌查询
        if (!"".equals(searchMap.get("brand")) && searchMap.get("brand") != null) {
            //不等于空说明有分类通过分类去查询
            FilterQuery filterQuery = new SimpleFilterQuery();
            //这里必须重新new这个条件对象
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按规格查询
        Map<String, String> specs = (Map) searchMap.get("spec");
        for (String key : specs.keySet()) {
            //这里循环  判断规格中是否有值存在
            FilterQuery filterQuery = new SimpleFilterQuery();
            //这里必须重新new这个条件对象                                               这里是获得每一个规格中的value
            Criteria filterCriteria = new Criteria("item_spec_" + key).is(specs.get(key));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按价格查询
      if (!"".equals(searchMap.get("price"))){
          String price = (String) searchMap.get("price");
          String[] split = price.split("-");
          if (!"0".equals(split[0])){
              FilterQuery filterQuery = new SimpleFilterQuery();
              //这里必须重新new这个条件对象                                               这里是获得每一个规格中的value
              Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
              filterQuery.addCriteria(filterCriteria);
              query.addFilterQuery(filterQuery);
          }
          if (!"*".equals(split[1])){
              FilterQuery filterQuery = new SimpleFilterQuery();
              //这里必须重新new这个条件对象                                               这里是获得每一个规格中的value
              Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
              filterQuery.addCriteria(filterCriteria);
              query.addFilterQuery(filterQuery);
          }
      }
      //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
      if (pageNo==null){
          pageNo=1;
      }
        Integer pageSize = (Integer) searchMap.get("pageSize");
      if (pageSize==null){
          pageSize=10;
      }
      //设置查询的起始索引  和查询的总条数
        query.setOffset((pageNo-1)*pageSize);
      query.setRows(pageSize);
      //设置排序的字段和方式  因为点击了肯定会有排序的字段和排序的方式
        //判断排序的方式
        String sortFiled = (String) searchMap.get("sortFiled");
        String sort = (String) searchMap.get("sort");
        if (!"".equals(sort)&&!"".equals(sortFiled)){
            if ("ASC".equalsIgnoreCase(sort)){
                //这表示是升序
                Sort sort1=new Sort(Sort.Direction.ASC,"item_"+sortFiled);
                query.addSort(sort1);
            }else {
                //这是降序
                Sort sort1=new Sort(Sort.Direction.DESC,"item_"+ sortFiled);
                query.addSort(sort1);
            }
        }
        //根据新品排序 即商品的更新时间



        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for (HighlightEntry<TbItem> entry : page.getHighlighted()) {
            //获取到原实体类   这个相当于没有被高亮的东西
            TbItem item = entry.getEntity();
            if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
//                              获取到高亮的内容并将其设置进去
                item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("total", page.getTotalElements());
        map.put("items", page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        return map;
    }

    //更新索引库
    @Override
    public void importItemData(List<TbItem> items) {
    solrTemplate.saveBeans(items);
    solrTemplate.commit();
    }

    @Override
    public void deleItems(Long[] ids) {
        Query query = new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(ids);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

}
