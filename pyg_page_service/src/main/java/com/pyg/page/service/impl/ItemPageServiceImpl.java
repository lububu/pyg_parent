package com.pyg.page.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pageDir}")
    private String pageDir;
    //引入freemarker
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public void genItemHtml(Long goodsId) throws Exception {
    //这里需要用freemarker；
        Configuration configuration = freeMarkerConfig.getConfiguration();
        //设置模板
        Template template = configuration.getTemplate("item.ftl");
        Map dataModel = new HashMap();
        //查找goods的数据
        TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goods",goods);
//        根据传过来的去查商品的三级分类
        String itemCat1Id = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
        String itemCat2Id = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
        String itemCat3Id = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

        dataModel.put("itemCat1Id",itemCat1Id);
        dataModel.put("itemCat2Id",itemCat2Id);
        dataModel.put("itemCat3Id",itemCat3Id);
        //查找goodsdesc的数据
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc",goodsDesc);
        //查找item的数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<TbItem> items = itemMapper.selectByExample(example);
        dataModel.put("items",items);
        //查找itemcat的数据

        //输出html页面
        FileWriter writer = new FileWriter(pageDir+goodsId+".html");
        template.process(dataModel,writer);
        writer.close();
    }
}
