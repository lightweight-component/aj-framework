package org.example.controller;

import com.ajaxjs.api.security.referer.HttpRefererCheck;
import org.example.model.Foo;

import org.example.model.User;
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

    @GetMapping("/user")
    User User();

}
