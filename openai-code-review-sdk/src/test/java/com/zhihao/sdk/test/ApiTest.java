package com.zhihao.sdk.test;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.zhihao.sdk.domain.model.ChatCompletionSyncResponse;
import com.zhihao.sdk.types.utils.BearerTokenUtils;
import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Jackson
 * @create 2025/6/19 10:53
 * @description 功能测试
 */

public class ApiTest {

    public static void main(String[] args) {
        String apiKeySecret = new String("54ec5157408547c1b3877d3f15e6759a.kcT9ohABVZJlhNf2");
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }

    /*
        HTTP 请求完整流程
            你的程序准备数据 → 输出到服务器 (getOutputStream())
            服务器接收处理 → 生成响应
            服务器返回数据 → 你的程序输入接收 (getInputStream())
     */
    @Test
    public void test_http() throws IOException {
        String apiKeySecret = "54ec5157408547c1b3877d3f15e6759a.kcT9ohABVZJlhNf2";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String code = "1+1";

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";

        // 程序🤔connection发送信息，所以是output
        try(OutputStream os = connection.getOutputStream()){
            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        // connection的服务端向我们程序发送信息，即程序为接收方，所以是input
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        // {"choices":[{"finish_reason":"stop","index":0,"message":{"content":"要评审代码 \"1+1\"，我们需要分析以下几个方面：\n\n1. **代码的功能性**：\n   - 代码是否完成了预期的功能？在这个案例中，代码的功能是将两个数字1相加。\n\n2. **代码的正确性**：\n   - 代码是否正确地实现了其功能？在这个例子中，\"1+1\" 应该正确计算出结果 2。\n\n3. **代码的简洁性**：\n   - 代码是否简洁明了？对于如此简单的操作，代码已经是相当简洁的。\n\n4. **代码的可读性**：\n   - 代码是否容易理解？即使是编程新手也应该能够一眼看出这段代码是在做加法。\n\n5. **代码的健壮性**：\n   - 代码是否考虑了各种可能的输入和边界条件？在这个例子中，输入非常确定，因此健壮性不是问题。\n\n以下是针对 \"1+1\" 代码的评审：\n\n- **功能性**：代码完全符合预期，将两个数字 1 相加，输出结果 2。\n\n- **正确性**：代码是正确的。在大多数编程语言中，\"1+1\" 将正确地计算为 2。\n\n- **简洁性**：代码简洁明了，没有冗余。\n\n- **可读性**：代码的可读性非常高，即使是编程新手也能轻松理解。\n\n- **健壮性**：由于 \"1+1\" 的输入非常确定，代码的健壮性在这个场景下不是问题。但如果要考虑扩展性，例如允许不同类型的数值输入，那么代码需要进一步的健壮性考虑。\n\n总结：代码 \"1+1\" 是一个完美的例子，展示了编程中如何实现简单而直接的功能。它简洁、正确且易于理解。然而，对于需要处理更多复杂输入和场景的应用，代码需要进一步的改进以增加其健壮性和可扩展性。","role":"assistant"}}],"created":1750835905,"id":"20250625151817c646e851539b4e05","model":"glm-4-flash","request_id":"20250625151817c646e851539b4e05","usage":{"completion_tokens":384,"prompt_tokens":41,"total_tokens":425}}
//        System.out.println(content.toString());

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());

    }



}
