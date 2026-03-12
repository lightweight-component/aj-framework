package com.ajaxjs.util.io;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Command Line Helper Utility - Provides methods for executing system commands
 * and processing their output through callback functions.
 *
 * <p>This class simplifies the execution of external commands and handles the
 * processing of command output with UTF-8 encoding. It includes proper exception
 * handling and logging for command execution failures.
 */
@Slf4j
public class CmdHelper {
    /**
     * 执行系统命令并处理输出结果
     *
     * @param cmd      需要执行的系统命令字符串
     * @param callback 处理命令输出每一行的回调函数，返回 false 时停止处理后续输出
     */
    public static void exec(String cmd, Function<String, Boolean> callback) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (ObjectHelper.hasText(line) && !callback.apply(line))
                        break;
                }
            }
            process.waitFor();
        } catch (IOException e) {
            log.warn("Executed command failed:" + cmd, e);
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.warn("Executed command failed:" + cmd, e);
            throw new RuntimeException(e);
        }
    }
}