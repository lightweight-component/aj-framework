package org.example.controller;

import com.ajaxjs.api.encryptbody.User;
import com.ajaxjs.api.security.referer.HttpRefererCheck;
import com.ajaxjs.api.time_signature.TimeSignatureVerify;
import org.example.model.Foo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foo")

public interface FooController {
    @GetMapping
    @HttpRefererCheck
    Foo getFoo();

    @GetMapping("/lock")
    boolean rLock();

    @PostMapping("/submit")
    boolean jsonSubmit(@RequestBody User user);
}
