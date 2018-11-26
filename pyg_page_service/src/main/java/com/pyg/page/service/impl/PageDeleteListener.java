package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.Serializable;

@Component
public class PageDeleteListener implements MessageListener {

    @Value("${pageDir}")
    private String pageDir;
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage= (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            for (Long id : ids) {
                String path=pageDir+id+".html";
             new File(path).delete();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
