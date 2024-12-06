package org.example.service;

import com.ajaxjs.model.MailVo;
import com.ajaxjs.service.message.ISendEmail;
import com.ajaxjs.service.tools.IIdCard;
import com.ajaxjs.springboot.annotation.JsonMessage;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;
import org.example.controller.FooController;
import org.example.model.Foo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FooService implements FooController {
    //    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Override
    @JsonMessage("返回 Foo")
//    @TimeSignatureVerify
    public Foo getFoo() {
//        RedisUtils.getInstance().set("bar", "888");
//        redisTemplate.opsForValue().set("foo", 1);
//        EntityManager entityManager = JpaUtil.getEntityManager();
//        entityManager.getTransaction().begin();
//
//        MyEntity entity = new MyEntity();
//        entity.setName("Example");
//        entityManager.persist(entity);
//
//        entityManager.getTransaction().commit();
//        entityManager.close();

        Foo foo = new Foo();
        foo.setName("hi");
//        throw new BusinessException("业务异常");

        return foo;
    }

    public static void main(String[] args) {
        ReferenceBuilder<Object> referenceBuilder = ReferenceBuilder.newBuilder();
        IIdCard demoService = (IIdCard) referenceBuilder.interfaceClass(IIdCard.class)
                .url("tri://localhost:50051")
                .build()
                .get();

        ISendEmail mailService = (ISendEmail) referenceBuilder.interfaceClass(ISendEmail.class)
                .url("tri://localhost:50051")
                .build()
                .get();

        boolean message = demoService.checkIdCard("440105198309060315");
        MailVo mail = new MailVo();
        mail.setTo("sp42@qq.com");
        mail.setSubject("hi");
        mail.setContent("test");
        mail.setFrom("frank@ajaxjs.com");

        boolean message2 = mailService.sendEmail(mail);


        System.out.println(message);
        System.out.println(message2);
    }
}
