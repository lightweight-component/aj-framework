package com.ajaxjs.base.sselog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class SseController {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 设置超时时间

        // 使用线程池异步发送事件
        executor.submit(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(i))
                            .name("sse-event")
                            .data("Event --" + i));
                    Thread.sleep(Duration.ofSeconds(1).toMillis()); // 等待1秒
                }
                emitter.complete(); // 完成流
            } catch (IOException e) {
                log.warn("Err", e);
                emitter.completeWithError(e); // 发生错误时完成流
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return emitter;
    }
}