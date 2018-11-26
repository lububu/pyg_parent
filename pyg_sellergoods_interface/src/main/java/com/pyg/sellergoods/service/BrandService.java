package com.pyg.sellergoods.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //    查找品牌
    public List<TbBrand> findAll();

    //  分页查找品牌
    public PageResult findPage(int pageNum, int pageSize);

    //添加品牌
    public void add(TbBrand brand);

    //修改品牌   第一实现回显 就是根据id查找品牌
    public TbBrand findOne(Long id);

    //第二步实现商品的修改
    public void update(TbBrand brand);

    //批量删除商品
    public void dele(Long[] ids);

    //查询操作
    public PageResult findPage(TbBrand brand,int pageNum, int pageSize);
    //查询品牌疯长的一个数组
    public  List<Map> selectOptionList();;
}
