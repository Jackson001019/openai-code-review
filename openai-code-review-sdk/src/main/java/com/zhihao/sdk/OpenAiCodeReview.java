package com.zhihao.sdk;

import com.alibaba.fastjson2.JSON;
import com.zhihao.sdk.domain.model.ChatCompletionRequest;
import com.zhihao.sdk.domain.model.ChatCompletionSyncResponse;
import com.zhihao.sdk.domain.model.Model;
import com.zhihao.sdk.types.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author Jackson
 * @create 2025/6/19 10:39
 * @description openai代码自动评审的java main主入口
 */
public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("OpenAiCodeReview Test Execute");

        /*执行 Git diff 操作并打印代码变更内容*/
        // 创建进程构建器执行 git diff 命令    git diff：Git差异比较命令 HEAD~1：上一次提交（当前提交的前一个版本） HEAD：当前最新提交
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 设置工作目录为当前目录
        processBuilder.directory(new File("."));

        // 启动外部进程
        Process process = processBuilder.start();

        // 创建缓冲读取器获取命令输出流
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();   // 用于存储差异内容
        // 逐行读取命令输出
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);  // 注意：这里没有保留换行符
//            diffCode.append(line).append("\n");  // 添加换行符
        }

        // 等待进程结束并获取退出码
        int exitCode = process.waitFor();
        System.out.println("Exited with code " + exitCode);
        // 打印差异内容
        System.out.println("Different code："+ diffCode.toString());

        String codeReviewResult = codeReview(diffCode.toString());
        System.out.println(codeReviewResult);

    }

    private static String codeReview(String diffCode) throws Exception {
        String apiKeySecret = "54ec5157408547c1b3877d3f15e6759a.kcT9ohABVZJlhNf2";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        // 构建chatglm请求request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {   // 匿名内部类
            // 类成员声明
            private static final long serialVersionUID = -7988151926241837899L;
            // 内层括号：实例初始化块
            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        // 程序🤔connection发送信息，所以是output
        try(OutputStream os = connection.getOutputStream()){
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("chatglm api response code: " + responseCode);

        // connection的服务端向我们程序发送信息，即程序为接收方，所以是input
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();
    }

}
