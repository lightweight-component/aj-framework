package org.example.service;

//import com.ajaxjs.desensitize.annotation.Desensitize;

import org.example.controller.FooController;
import org.example.model.Foo;
import org.example.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class FooService implements FooController {
    @Override
    public Foo getFoo() {
        return null;
    }

    @Override
    public boolean jsonSubmit(@RequestBody User user) {
        // 处理接收到的 user 对象
        System.out.println("Received user: " + user.getName() + ", " + user.getAge());
        return false;
    }

    @Override
    public User User() {
        User user = new User();
        user.setAge(1);
        user.setName("tom");

        return user;
    }

    @Override
//    @Desensitize
    public User UserDesensitize() {
        User user = new User();
        user.setAge(1);
        user.setName("tom");
        user.setPhone("13711118120");

        return user;
    }

    @Override
    public boolean testOpenFeign() {
        return false;
    }
}
