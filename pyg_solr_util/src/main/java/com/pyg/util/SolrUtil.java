package com.pyg.util;


import com.alibaba.fastjson.JSON;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//这个类的作用是查询数据库的数据，然后提交到solr中
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public static void main(String[] args) {

//      交给spring去new  SolrUtil solrUtil = new SolrUtil();
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importDate();
    }

    public void importDate(){
        //需要添加查询的条件 只要状态是1正常即可
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> items = itemMapper.selectByExample(example);
        for (TbItem item : items) {
            //获取到spe中的数据设置到specMap中
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
            System.out.println(item.getSpecMap());
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }
}
