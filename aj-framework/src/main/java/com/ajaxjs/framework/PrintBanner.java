package com.ajaxjs.framework;

import com.ajaxjs.util.StrUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * 在项目启动时定制化一些附加功能，比如：加载一些系统参数、完成初始化、预热本地缓存
 * <a href="https://blog.csdn.net/interestANd/article/details/129257076">...</a>
 * <a href="https://juejin.cn/post/7083680843696340999">...</a>
 * <a href="https://my.oschina.net/qq596392912/blog/748829">...</a>
 */
@Component
public class PrintBanner implements ApplicationRunner {
    /**
     * Spring 程序启动的时间
     */
    public static final long APP_START_TIME = System.currentTimeMillis();

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.servlet.context-path: }")
    private String contextPath;

    @Value("${server.isShowBanner:true}")
    private boolean isShowBanner;
    private static final String bannerName = " :: framework.ajaxjs.com :: ";

    private String version = "1.0";

    private static final String[] BANNER = {
            "",
            "\033[91m       ___       _       ___  __    __      _   _____        _          __  _____   _____  ",
            "\033[92m     /   |     | |     /   | \\ \\  / /     | | /  ___/      | |        / / | ____| |  _  \\ ",
            "\033[93m    / /| |     | |    / /| |  \\ \\/ /      | | | |___       | |  __   / /  | |__   | |_| |  ",
            "\033[94m   / / | |  _  | |   / / | |   }  {    _  | | \\___  \\      | | /  | / /   |  __|  |  _  {  ",
            "\033[95m  / /  | | | |_| |  / /  | |  / /\\ \\  | |_| |  ___| |      | |/   |/ /    | |___  | |_| |  ",
            "\033[96m /_/   |_| \\_____/ /_/   |_| /_/  \\_\\ \\_____/ /_____/      |___/|___/     |_____| |_____/ ",
            "\033[0m"
    };

    private static final String s = ("\n     ___       _       ___  __    __      _   _____        _          __  _____   _____  \n"
            + "     /   |     | |     /   | \\ \\  / /     | | /  ___/      | |        / / | ____| |  _  \\ \n"
            + "    / /| |     | |    / /| |  \\ \\/ /      | | | |___       | |  __   / /  | |__   | |_| |  \n"
            + "   / / | |  _  | |   / / | |   }  {    _  | | \\___  \\      | | /  | / /   |  __|  |  _  {  \n"
            + "  / /  | | | |_| |  / /  | |  / /\\ \\  | |_| |  ___| |      | |/   |/ /    | |___  | |_| |  \n"
            + " /_/   |_| \\_____/ /_/   |_| /_/  \\_\\ \\_____/ /_____/      |___/|___/     |_____| |_____/ \n");

    @Override
    public void run(ApplicationArguments args) {
//        log.info(s);
        long elapsedTime = System.currentTimeMillis() - APP_START_TIME;
//        log.info("Spring App:{} startup time: {} ms", port, elapsedTime);

        Log log = LogFactory.getLog(PrintBanner.class);
        log.info("Spring App: " + contextPath + ":" + port + " startup time: " + elapsedTime + " ms");

        if (isShowBanner) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 PrintStream printStream = new PrintStream(baos)) {


                for (String s2 : BANNER)
                    printStream.println(s2);


                version = (version != null) ? " (v" + version + ")" : StrUtil.EMPTY_STRING;

                StringBuilder padding = new StringBuilder();
                while (padding.length() < 24 - (version.length() + bannerName.length()))
                    padding.append(" ");

                printStream.println(AnsiOutput.toString(AnsiColor.GREEN, bannerName, AnsiColor.DEFAULT, padding.toString(), AnsiStyle.FAINT, version));
                log.info(baos.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}