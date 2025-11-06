package com.ajaxjs.wechat.applet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AppletUserService {
    @PostMapping("/login")
    String login(@RequestParam(required = true) String code);
}
