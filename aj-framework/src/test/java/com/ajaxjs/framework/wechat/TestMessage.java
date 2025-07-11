package com.ajaxjs.framework.wechat;

import com.ajaxjs.framework.wechat.applet.GetToken;
import com.ajaxjs.framework.wechat.applet.Message;
import com.ajaxjs.framework.wechat.applet.WeChatAppletConfig;
import com.ajaxjs.framework.wechat.applet.model.TemplateMsgMp;
import com.ajaxjs.framework.wechat.applet.model.TemplateMsgWebApp;
import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestMessage {
    Message m;

    {
        WeChatAppletConfig cfg = new WeChatAppletConfig();

        GetToken getToken = new GetToken(cfg);
        getToken.init();

        m = new Message(cfg);
    }

    @Test

    public void testSend() {
        TemplateMsgMp msg = new TemplateMsgMp();
        msg.setAppid("wx450b94ff4d18b540");
        msg.setTemplate_id("f46S5vuzp4aB3OEuCNDJendMs3HuoIALWqVDjJWwS5Y");
        msg.setUrl("http://foo");

        Map<String, Object> params = new HashMap<>();
        params.put("first", ObjectHelper.mapOf("value", "您有一条订单产生预警消息，请尽快安排处理。"));
        params.put("keyword1", ObjectHelper.mapOf("value", "无"));
        params.put("keyword2", ObjectHelper.mapOf("value", "date"));
        params.put("keyword3", ObjectHelper.mapOf("value", "warningType"));
        params.put("remark", ObjectHelper.mapOf("value", "详细请至“车辆租赁”运营后台序查看并处理。"));
        msg.setData(params);


        TemplateMsgMp.Miniprogram miniprogram = new TemplateMsgMp.Miniprogram();
        msg.setMiniprogram(miniprogram);
        miniprogram.setAppid("wx450b94ff4d18b540");
        miniprogram.setPagepath("");

        TemplateMsgWebApp webAppMsg = new TemplateMsgWebApp();
        webAppMsg.setTemplate_id("f46S5vuzp4aB3OEuCNDJendMs3HuoIALWqVDjJWwS5Y");
        webAppMsg.setForm_id("");

//        m.send("oacQ60yWx4Hh2eQ0Ye1a7vZLgugw", msg, null);
    }

    @Test
    public void testGetCategory() {
        m.getCategory();
    }

    @Test
    public void testGetPubTemplateKeyWordsById() {
        m.getPubTemplateKeyWordsById("129");
    }

    @Test
    public void testGetPubTemplateTitleList() {
        m.getPubTemplateTitleList("129");
    }

    @Test
    public void testGetMessageTemplateList() {
        m.getMessageTemplateList();
    }
}
