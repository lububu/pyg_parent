package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        //接收消息即可  根据消息传输的类型解析消息
        try {
            TextMessage textMessage= (TextMessage) message;
            List<TbItem> items = JSON.parseArray(textMessage.getText(), TbItem.class);
            //将其上传到solr索引库中
            itemSearchService.importItemData(items);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
