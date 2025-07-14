package com.zhihao.sdk;

import com.alibaba.fastjson2.JSON;
import com.zhihao.sdk.domain.model.ChatCompletionRequest;
import com.zhihao.sdk.domain.model.ChatCompletionSyncResponse;
import com.zhihao.sdk.domain.model.Message;
import com.zhihao.sdk.domain.model.Model;
import com.zhihao.sdk.types.utils.BearerTokenUtils;
import com.zhihao.sdk.types.utils.WXAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Jackson
 * @create 2025/6/19 10:39
 * @description openai代码自动评审的java main主入口
 */
public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("OpenAiCodeReview Test Execute");

        String token = System.getenv("GITHUB_TOKEN");
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }

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
        System.out.println("Code review log: " + codeReviewResult);

        // 3. 写入评审日志
        String logUrl = writeLog(token, codeReviewResult);
        System.out.println("Write log url：" + logUrl);

        // 4. 微信模版消息通知
        System.out.println("pushMessage：" + logUrl);
        pushMessage(logUrl);
    }

    private static void pushMessage(String logUrl) {
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("project", "big-market");
        message.put("review", logUrl);
        message.setUrl(logUrl);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 调用智谱AI模型自动根据‘差异代码’实现‘自动代码评审’
     * @Param [diffCode] git获取的差异代码
     * @Return java.lang.String 评审意见
     * @Date 2025/6/26
     **/
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

    /**
     * 将日志内容写入 GitHub 仓库
     * @Param [token, log] [GitHub 个人访问令牌 (用于认证)， 评审意见]
     * @Return java.lang.String 新创建文件的 GitHub 原始 URL
     * @Date 2025/6/26
     **/
    private static String writeLog(String token, String log) throws Exception {
        // 使用JGit库克隆目标仓库
        Git git = Git.cloneRepository()
                .setURI("https://github.com/Jackson001019/openai-code-review-log.git")
                .setDirectory(new File("repo")) // 设置本地存储路径为repo目录
                // 使用Github token进行认证（用户名为token 密码留空）
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        // 按日期创建存储目录
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        // 生成随机文件名并写入文件
        String fileName = generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
        }

        // 添加文件到暂存区
        // addFilepattern()方法：该方法接受的是相对于Git仓库根目录的路径，前面JGit已经setDirectory=repo，所以不需要带前缀
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        // 提交更改
        git.commit().setMessage("Add new file via GitHub Actions").call();
        // 推送到远程仓库
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        System.out.println("Changes have been pushed to the repository.");

        // 生成文件 URL
        return "https://github.com/Jackson001019/openai-code-review-log/blob/master/" + dateFolderName + "/" + fileName;
    }

    /**
     * 随机字符串生成方法
     * @Param [length]
     * @Return java.lang.String
     * @Date 2025/6/26
     **/
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }


}
