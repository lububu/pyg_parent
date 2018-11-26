package com.pyg.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbSeller;
import com.pyg.sellergoods.service.SellerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("login")
    public Map login(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeller seller = sellerService.findOne(name);
        Map map=new HashMap();
        map.put("name",seller.getNickName());
        return map;
    }
}
