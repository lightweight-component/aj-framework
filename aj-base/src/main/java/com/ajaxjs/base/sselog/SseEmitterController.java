package com.ajaxjs.base.sselog;

import com.ajaxjs.springboot.annotation.IgnoredGlobalReturn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sseEmitter")
public class SseEmitterController {
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
     * 服务端使用SseEmitter时使用
     *
     * @param username
     * @return
     * @throws IOException
     */
    @IgnoredGlobalReturn
    @GetMapping(value = "/connect/{username}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter connect(@PathVariable String username) throws IOException {
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(() -> {
            System.out.println(username + "连接结束！");
            emitterMap.remove(username);
        });
        sseEmitter.onError((t) -> {
            System.out.println(username + "连接出错！错误信息：" + t.getMessage());
            emitterMap.remove(username);
        });
        sseEmitter.onTimeout(() -> {
            System.out.println(username + "连接超时！");
            emitterMap.remove(username);
        });
        emitterMap.put(username, sseEmitter);

        sseEmitter.send("连接建立成功");

        return sseEmitter;
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
    public String send(@PathVariable String username) throws IOException {
        SseEmitter sseEmitter = emitterMap.get(username);
        if (sseEmitter == null) {
            return "没查询到该用户的连接！";
        }
        sseEmitter.send(SseEmitter.event().name("psh").data("Hello～"));
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
        for (SseEmitter sseEmitter : emitterMap.values()) {
            sseEmitter.send(SseEmitter.event().name("psh").data("Hello～"));
        }
        return "发送完成～";
    }
}
