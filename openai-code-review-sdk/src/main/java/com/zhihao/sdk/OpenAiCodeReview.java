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
        System.out.println("review code："+ diffCode.toString());
    }

}
