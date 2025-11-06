package com.ajaxjs.framework.license.context;

import com.ajaxjs.util.io.CmdHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 硬件指纹获取工具类
 * 支持Windows和Linux系统的主板序列号获取
 */
@Slf4j
public class HardwareUtil {
    /**
     * 获取主板序列号
     *
     * @return 主板序列号，获取失败返回"UNKNOWN"
     */
    public static String getMotherboardSerial() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("windows"))
                return getWindowsMotherboardSerial();
            else if (os.contains("linux"))
                return getLinuxMotherboardSerial();
            else {
                log.warn("不支持的操作系统: {}", os);
                return "UNKNOWN";
            }
        } catch (Exception e) {
            log.error("获取主板序列号失败", e);
            return "UNKNOWN";
        }
    }

    /**
     * 获取Windows系统主板序列号
     */
    private static String getWindowsMotherboardSerial() {
        AtomicReference<String> result = new AtomicReference<>();
        result.set("UNKNOWN");

        CmdHelper.exec("wmic baseboard get serialnumber", line -> {
            if (!line.equals("SerialNumber")) {
                result.set(line);
                return false;
            } else
                return true;
        });

        if ("Default string".equalsIgnoreCase(result.get()))
            CmdHelper.exec("wmic csproduct get UUID", line -> {

                if (!line.equals("UUID")) {
                    result.set(line);
                    return false;
                } else
                    return true;
            });

        return result.get();
    }

    /**
     * 获取Linux系统主板序列号
     */
    private static String getLinuxMotherboardSerial() {
        try {
            // 尝试通过dmidecode命令获取
            Process process = Runtime.getRuntime().exec("sudo dmidecode -s baseboard-serial-number");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line = reader.readLine();
            reader.close();
            process.waitFor();

            if (line != null && !line.trim().isEmpty() && !line.contains("Not Specified")) {
                log.debug("Linux主板序列号: {}", line.trim());
                return line.trim();
            }


            return getLinuxMotherboardFromSys();// 如果dmidecode失败，尝试读取/sys/class/dmi/id/board_serial
        } catch (Exception e) {
            log.error("获取Linux主板序列号失败", e);
            return getLinuxMotherboardFromSys();
        }
    }

    /**
     * 从/sys/class/dmi/id/board_serial读取主板序列号
     */
    private static String getLinuxMotherboardFromSys() {
        try {
            Process process = Runtime.getRuntime().exec("cat /sys/class/dmi/id/board_serial");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line = reader.readLine();
            reader.close();
            process.waitFor();

            if (line != null && !line.trim().isEmpty()) {
                log.debug("Linux主板序列号(从sys读取): {}", line.trim());
                return line.trim();
            }

        } catch (Exception e) {
            log.warn("从/sys/class/dmi/id/board_serial读取失败", e);
        }

        return "UNKNOWN";
    }

    /**
     * 获取系统信息摘要（用于调试）
     */
    public static String getSystemInfo() {
        return String.format("OS: %s, Arch: %s, Motherboard: %s",
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                getMotherboardSerial()
        );
    }

    public static void main(String[] args) {
        System.out.println(HardwareUtil.getMotherboardSerial());
    }
}