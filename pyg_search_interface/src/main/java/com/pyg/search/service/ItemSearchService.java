package com.pyg.search.service;

import com.pyg.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map<String,Object> search(Map searchMap);

    //更新索引库
    public void importItemData(List<TbItem> items);

    public void deleItems(Long[] ids);
}
