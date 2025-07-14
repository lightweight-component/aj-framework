package com.ajaxjs.base.service.watchlog.impl;

import com.ajaxjs.base.service.watchlog.BaseTail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Get the log by Linux command tail -f
 * Linux only
 */
public class TailLog extends BaseTail {
    private BufferedReader reader;

    public TailLog() {
        // 执行tail -f命令
        Process process;

        try {
            process = Runtime.getRuntime().exec("tail -f /var/log/syslog");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputStream inputStream = process.getInputStream();
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                // 将实时日志通过WebSocket发送给客户端，给每一行添加一个HTML换行
//                session.getBasicRemote().sendText(line + "<br>");
                setMessageLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
