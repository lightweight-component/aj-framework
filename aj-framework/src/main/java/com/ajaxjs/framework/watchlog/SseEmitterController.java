package com.ajaxjs.framework.watchlog;

import com.ajaxjs.framework.mvc.unifiedreturn.IgnoredGlobalReturn;
import com.ajaxjs.framework.watchlog.impl.ReadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sseEmitter")
@Slf4j
public class SseEmitterController {
    private Long timeout = 3 * 1000L;

    private static final Map<String, SseEmitter> emitterMap = new HashMap<>();

    /**
     * 服务端不使用SseEmitter时使用
     *
     * @param response
     * @throws IOException
     */
    @GetMapping(value = "/data")
    public void getData(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream;charset=UTF-8");
        response.getWriter().write("retry: 5000\n");
        response.getWriter().write("data: hahahaha\n\n");
        response.getWriter().flush();
        System.in.read();
    }

    /**
     * 服务端使用 SseEmitter 时使用
     *
     * @param username
     * @return
     * @throws IOException
     */
    @IgnoredGlobalReturn
    @GetMapping(value = "/connect/{username}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter connect(@PathVariable String username) {
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> {
            System.out.println(username + "连接结束！");
            emitterMap.remove(username);
        });
        emitter.onError((t) -> {
            System.out.println(username + "连接出错！错误信息：" + t.getMessage());
            emitterMap.remove(username);
        });
        emitter.onTimeout(() -> {
            System.out.println(username + "连接超时！");
            emitterMap.remove(username);
        });
        emitterMap.put(username, emitter);

        try {
            emitter.send("连接建立成功");
        } catch (IOException e) {
            emitter.completeWithError(e); // 发生错误时完成流
            throw new UncheckedIOException(e);
        }

        return emitter;
    }

    /**
     * 服务端使用SseEmitter时使用
     *
     * @param username
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/send/{username}")
    @IgnoredGlobalReturn
    public String send(@PathVariable String username) {
        SseEmitter sseEmitter = emitterMap.get(username);
        if (sseEmitter == null)
            return "没查询到该用户的连接！";

        try {
            sseEmitter.send(SseEmitter.event().name("psh").data("Hello～"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "发送成功～";
    }

    /**
     * 服务端使用SseEmitter时使用
     *
     * @return
     * @throws IOException
     */
    @IgnoredGlobalReturn
    @GetMapping(value = "/sendAll")
    public String sendAll() throws IOException {
        for (SseEmitter sseEmitter : emitterMap.values())
            sseEmitter.send(SseEmitter.event().name("psh").data("Hello～"));

        return "发送完成～";
    }

    String p = "C:\\code\\ajaxjs\\aj-framework\\aj-base\\src\\test\\java\\com\\ajaxjs\\base\\watchlog\\bar.txt";
    ReadFile tail;

    @IgnoredGlobalReturn
    @GetMapping(value = "/tail_log")
    public String tailLog() {
        if (tail == null) {
            tail = new ReadFile(p, 1000, true);
            tail.setTailing(true);
            tail.start();
        }
        try {
            for (SseEmitter sseEmitter : emitterMap.values()) {
                sseEmitter.send(SseEmitter.event().name("psh").data(tail.getMessageLine()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tail.setMessageLine("");

        return "all-ok";
    }
}
