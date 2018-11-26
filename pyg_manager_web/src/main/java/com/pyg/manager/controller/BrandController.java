package com.pyg.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbBrand;
import com.pyg.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    @RequestMapping("findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("findPage")
    public PageResult findPage(int page, int rows) {
        return brandService.findPage(page, rows);
    }

    @RequestMapping("add")
    public Result add(@RequestBody TbBrand brand) {
        //通过try catch来判断是否添加成公   细节！
        try {
            brandService.add(brand);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            return new Result(false, "添加失败");
        }
    }
    //回显查找单个商品
    @RequestMapping("findOne")
    public TbBrand findOne(Long id){
       return brandService.findOne(id);
    }

    @RequestMapping("update")
    public Result update(@RequestBody TbBrand brand) {
        //通过try catch来判断是否添加成公   细节！   修改商品
        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("dele")
    public Result dele(Long[] ids) {
        //通过try catch来判断是否添加成公   细节！   修改商品
        try {
            brandService.dele(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("search")
    public PageResult search(@RequestBody TbBrand brand,int page, int rows) {
        return brandService.findPage(brand,page, rows);
    }
    @RequestMapping("selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }


}
