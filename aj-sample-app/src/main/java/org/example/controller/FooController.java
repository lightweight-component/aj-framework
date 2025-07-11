package org.example.controller;

//import com.ajaxjs.desensitize.annotation.Desensitize;

import com.ajaxjs.security.referer.HttpRefererCheck;
import org.example.model.Foo;
import org.example.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foo")

public interface FooController {
    @GetMapping
    @HttpRefererCheck
    Foo getFoo();

    @PostMapping("/submit")
    boolean jsonSubmit(@RequestBody User user);

    @GetMapping("/user")
    User User();

    @GetMapping("/user_desensitize")
    User UserDesensitize();

    @GetMapping("/feign")
    boolean testOpenFeign();
}
