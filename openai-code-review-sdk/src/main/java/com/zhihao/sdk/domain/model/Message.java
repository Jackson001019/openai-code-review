package com.zhihao.sdk.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信模版消息通知接口 请求类
 */
public class Message {

    // 被通知用户
    private String touser = "oUhRAvuypO29MGihBBV3UbXdOtg0";
    // 模版消息id
    private String template_id = "9SjU31fUtjG3bTw31CyudEZx8hP1eHP-OnlrR4eOM0A";
    // 模版消息点击后跳转页面url
    private String url = "https://weixin.qq.com";
    // 模版消息的数据
    private Map<String, Map<String, String>> data = new HashMap<>();

    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }

}
