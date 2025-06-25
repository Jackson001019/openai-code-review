package com.zhihao.sdk.domain.model;

import java.util.List;

public class ChatCompletionRequest {

    // 要调用的模型编码
    private String model = Model.GLM_4_FLASH.getCode();
    // 提示prompt列表
    private List<Prompt> messages;

    public static class Prompt {
        /*
            role：消息角色
            system-系统角色-全局对话提示（作用于整个对话上下文）
            user-用户消息-仅限当前交互，不具备全局能力。
         */
        private String role;
        // 提问内容
        private String content;

        public Prompt() {
        }

        public Prompt(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Prompt> getMessages() {
        return messages;
    }

    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }
}
