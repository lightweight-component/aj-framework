package com.ajaxjs.framework.sysmonitor;

import com.ajaxjs.framework.sysmonitor.model.LinuxMemInfo;
import com.ajaxjs.util.io.DataReader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.StringTokenizer;

/**
 * 利用 runtime.exec() 获取CPU利用率、内存使用情况及CPU温度
 *
 * @author <a href="https://blog.csdn.net/hj7jay/article/details/51979939">...</a>
 */
@Slf4j
public class LinuxSystemTool {
    /**
     * 内存的信息，单位 kb
     */
    public static LinuxMemInfo getMemInfo() {
        LinuxMemInfo info = new LinuxMemInfo();

        try {
            DataReader dataReader = new DataReader(getFileInputStream("/proc/meminfo"), StandardCharsets.UTF_8);
            dataReader.readAsLineString(line -> {
                StringTokenizer token = new StringTokenizer(line);

                if (!token.hasMoreTokens())
                    return;

                line = token.nextToken();

                if (!token.hasMoreTokens())
                    return;

                if (line.equalsIgnoreCase("MemTotal:"))
                    info.setTotal(token.nextToken());
                else if (line.equalsIgnoreCase("MemFree:"))
                    info.setFree(token.nextToken());
                else if (line.equalsIgnoreCase("SwapTotal:"))
                    info.setSwapTotal(token.nextToken());
                else if (line.equalsIgnoreCase("SwapFree:"))
                    info.setSwapFree(token.nextToken());
            });
        } catch (IOException e) {
            log.warn("Errors when getting memory info.", e);
            throw new UncheckedIOException(e);
        }

        return info;
    }

    private static InputStream getFileInputStream(String filePath) throws IOException {
        return Files.newInputStream(new File(filePath).toPath());
    }

    private static BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getFileInputStream("/proc/stat")));
    }

    /**
     * Get CPU usage
     *
     * @return CPU 使用率
     */
    public static float getCpuUsage() {
        StringTokenizer token;
        int user1, nice1, sys1, idle1;

        try (BufferedReader br = getReader()) {
            token = new StringTokenizer(br.readLine());
            token.nextToken();

            user1 = Integer.parseInt(token.nextToken());
            nice1 = Integer.parseInt(token.nextToken());
            sys1 = Integer.parseInt(token.nextToken());
            idle1 = Integer.parseInt(token.nextToken());
        } catch (IOException e) {
            log.warn("Errors when getting CPU Usage.", e);
            return 0;
        }

        sleep(1);

        try (BufferedReader br = getReader()) {
            token = new StringTokenizer(br.readLine());
            token.nextToken();

            int user2 = Integer.parseInt(token.nextToken());
            int nice2 = Integer.parseInt(token.nextToken());
            int sys2 = Integer.parseInt(token.nextToken());
            int idle2 = Integer.parseInt(token.nextToken());

            return (float) ((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float) ((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
        } catch (IOException e) {
            log.warn("Errors when getting CPU Usage.", e);
            return 0;
        }
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            log.warn("Errors when sleeping.", e);
        }
    }
}