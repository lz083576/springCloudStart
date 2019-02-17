package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class PageDeleteListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodIds = (Long[]) objectMessage.getObject();
            System.out.println("接收到要删除的信息");
            boolean b = itemPageService.deleteItemHtml(goodIds);
            System.out.println("删除结果"+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
