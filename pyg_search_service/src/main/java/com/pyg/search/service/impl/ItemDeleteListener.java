package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        //接收消息
        try {
            ObjectMessage objectMessage= (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            //循环从索引库中删除
            itemSearchService.deleItems(ids);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
