package com.ajaxjs.devtools.sysmonitor;

import com.ajaxjs.util.io.StreamHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 利用runtime.exec()获取CPU利用率、内存使用情况及CPU温度
 *
 * @author <a href="https://blog.csdn.net/hj7jay/article/details/51979939">...</a>
 */
@Slf4j
public class LinuxSystemTool {
    /**
     * 内存的信息(kb)
     */
    public static Map<String, Object> getMemInfo() {
        File file = new File("/proc/meminfo");
        Map<String, Object> map = new HashMap<>();

        try {
            StreamHelper.read(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8, line -> {
                StringTokenizer token = new StringTokenizer(line);

                if (!token.hasMoreTokens())
                    return;

                line = token.nextToken();

                if (!token.hasMoreTokens())
                    return;

                if (line.equalsIgnoreCase("MemTotal:"))
                    map.put("MemTotal", token.nextToken());
                else if (line.equalsIgnoreCase("MemFree:"))
                    map.put("MemFree", token.nextToken());
                else if (line.equalsIgnoreCase("SwapTotal:"))
                    map.put("SwapTotal", token.nextToken());
                else if (line.equalsIgnoreCase("SwapFree:"))
                    map.put("SwapFree", token.nextToken());
            });
        } catch (IOException e) {
            log.warn("Errors when getting memory info.", e);
            throw new UncheckedIOException(e);
        }

        return map;
    }

    /**
     * get memory by used info
     *
     * @return float efficiency
     */
    public static float getCpuInfo() {
        File file = new File("/proc/stat");
        StringTokenizer token;
        int user1, nice1, sys1, idle1;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())))) {
            token = new StringTokenizer(br.readLine());
            token.nextToken();

            user1 = Integer.parseInt(token.nextToken());
            nice1 = Integer.parseInt(token.nextToken());
            sys1 = Integer.parseInt(token.nextToken());
            idle1 = Integer.parseInt(token.nextToken());
        } catch (IOException e) {
            log.warn("Errors when getting CPU info.", e);
            return 0;
        }

        Utils.sleep(1);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())))) {
            token = new StringTokenizer(br.readLine());
            token.nextToken();

            int user2 = Integer.parseInt(token.nextToken());
            int nice2 = Integer.parseInt(token.nextToken());
            int sys2 = Integer.parseInt(token.nextToken());
            int idle2 = Integer.parseInt(token.nextToken());

            return (float) ((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float) ((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
        } catch (IOException e) {
            log.warn("Errors when getting CPU info.", e);
            return 0;
        }

    }
}