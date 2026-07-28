package com.ajaxjs.framework.livelog;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogFileTailer extends Thread {
    /**
     * 读取时间间隔
     */
    private long sampleInterval = 1000;

    /**
     * 是否显示文件头？还是说只显示后面变化的部分
     */
    private boolean startAtBeginning = true;

    private final File logfile;

    /**
     * 回调事件
     */
    private Consumer<String> listener;

    /**
     * 监视开关，true = 打开监视
     */
    private boolean tailing;

    /**
     * @param file 要监视的文本文件
     */
    public LogFileTailer(String file) {
        logfile = new File(file);
    }

    @Override
    public void run() {
        long filePointer = startAtBeginning ? 0 : logfile.length();

        try {
            RandomAccessFile file = new RandomAccessFile(logfile, "r");

            while (tailing) {
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

                        if (listener != null)
                            listener.accept(line);

                        line = file.readLine();
                    }

                    filePointer = file.getFilePointer();
                }

                sleep(sampleInterval);
            }

            file.close();
        } catch (IOException | InterruptedException e) {

        }
    }

    public static void main(String[] args) throws IOException {
        LogFileTailer tailer = new LogFileTailer("C:\\temp\\bar.txt");
        tailer.setTailing(true);
        tailer.setListener(System.out::println);
        tailer.start();
    }

}