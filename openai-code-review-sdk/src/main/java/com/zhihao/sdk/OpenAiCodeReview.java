package com.zhihao.sdk;

import java.io.*;

/**
 * @author Jackson
 * @create 2025/6/19 10:39
 * @description openai代码自动评审的java main主入口
 */
public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        System.out.println("OpenAiCodeReview Test Execute");

        // 1. 检出github最近2次代码记录
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code " + exitCode);

        System.out.println("review code："+ diffCode.toString());
    }

}
