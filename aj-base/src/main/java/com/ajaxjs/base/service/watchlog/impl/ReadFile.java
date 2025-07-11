package com.ajaxjs.base.service.watchlog.impl;

import com.ajaxjs.base.service.watchlog.BaseTail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Get the log by reading log file
 * for Win/Linux
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReadFile extends BaseTail {
    private final File logfile;

    private boolean startAtBeginning;

    /**
     * @param file             要监视的文本文件
     * @param interval         读取时间间隔
     * @param startAtBeginning 是否显示文件头？还是说只显示后面变化的部分
     */
    public ReadFile(String file, long interval, boolean startAtBeginning) {
        this(file, startAtBeginning);
        setInterval(interval);
    }

    /**
     * @param file             要监视的文本文件
     * @param startAtBeginning 是否显示文件头？还是说只显示后面变化的部分
     */
    public ReadFile(String file, boolean startAtBeginning) {
        logfile = new File(file);
        this.startAtBeginning = startAtBeginning;
    }

    @Override
    public void run() {
        long filePointer = startAtBeginning ? 0 : logfile.length();
        RandomAccessFile file = null;

        try {
            file = new RandomAccessFile(logfile, "r");

            while (isTailing()) {
                long fileLength = logfile.length();

                if (fileLength < filePointer) {
                    file = new RandomAccessFile(logfile, "r");
                    filePointer = 0;
                }

                if (fileLength > filePointer) {
                    file.seek(filePointer);
                    String line = file.readLine();

                    while (line != null) {
                        line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                        setMessageLine(line);
                        if (getCallback() != null)
                            getCallback().accept(line);

                        line = file.readLine();
                    }

                    filePointer = file.getFilePointer();
                }

                sleep(getInterval());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null)
                    file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}