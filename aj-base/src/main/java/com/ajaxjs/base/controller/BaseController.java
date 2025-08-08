package com.ajaxjs.base.controller;

import com.ajaxjs.framework.mvc.unifiedreturn.ResponseResultWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class BaseController {
    @GetMapping
    public ResponseResultWrapper test() {
        return new ResponseResultWrapper().setStatus(1).setData("Welcome to AJ-base!");
    }
}
