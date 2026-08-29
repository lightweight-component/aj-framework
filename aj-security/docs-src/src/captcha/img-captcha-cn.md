---
title: 图片验证码
subTitle: 2024-12-05 by Frank Cheung
description: 图片验证码
date: 2022-01-05
tags:
  - captcha
  - 图片验证码
layout: layouts/docs-cn.njk
---

# 图片验证码

图片验证码能够防止机器人注册、灌水等的恶意操作。一般在开放接口进行写入操作的时候需要添加保护的。

# 使用方式

## YAML 配置

```yaml
security:
  ImageCaptcha: # 图片验证码
    enabled: true
    expireSeconds: 60
    message:
      zh_CN:
        a1: 验证码错误
        a2: 参数 [%s] 必填
        a3: 缺少参数 'uuid'
        a4: 不存在图片验证码或者已经过期，请稍后再试
      en_US:
        a1: The captcha code is incorrect.
        a2: The parameter %s is required.
        a3: The parameter 'uuid' is required.
        a4: The captcha code doesn't exist or expired. Please try to generate it again.
```

## 配置

创建一个配置类，并添加到 Spring 容器中。主要是决定哪种图片生成器以及缓存方式。这里简单起见使用了 JVM 的缓存 SimpleCache。
如果要改为 Redis 可以参照下面方式来配置`SaveToRam`、`CaptchaCodeFromRam`、`RemoveByKey`。

```java
static final SimpleCache RAM = new SimpleCache(); // JVM 缓存

@Bean
ImageCaptchaConfig ImageCaptchaConfig() {
    ImageCaptchaConfig config = new ImageCaptchaConfig();
    config.setCaptchaImageProvider(new SimpleCaptchaImage());
    config.setSaveToRam(RAM::add);
    config.setCaptchaCodeFromRam(key -> {
        SimpleCache.Item item = RAM.get(key);
        return item == null ? null : item.getValue();
    });
    config.setRemoveByKey(RAM::remove);

    return config;
}
```

## 添加控制器

首先我们把验证码图片显示出来，添加一个Spring 接口，返回图片验证码：

```java
@Autowired
ImageCaptcha imageCaptcha;

@GetMapping("/captcha")
@IgnoreDataBaseConnect // 不需要连接数据库的注解，视乎你的框架所定
void showCaptcha(HttpServletRequest req, HttpServletResponse response) {
    imageCaptcha.captchaImage(req, response);
}
```

前端请求该接口须携带一个`uuid`，表明本次生成验证码的唯一标识。建议使用 QueryString 传参的方式，例如`/captcha?uuid=xxx`。

### 前端引入验证码

下面用原生 JS 写出前端的代码：

```html
<input type="text" class="captcha-code" placeholder="请输入验证码" style="width:40%" />
<img src="http://localhost:8089/base_api/feedback/captcha" class="captcha-img" title="单击刷新"
    style="cursor: pointer;vertical-align: middle;" />
<script>
    window.CAPTCHA_UUID; // 提交时需要携带的验证码 UUID，QueryString 形式传参，避免与表单实体耦合

    ; (function () {
        const imgApi = 'http://localhost:8089/base_api/feedback/captcha';
        const captchaImg = document.querySelector('.captcha-img');

        function init() {
            window.CAPTCHA_UUID = getUUID();
            captchaImg.src = `${imgApi}?uuid=${window.CAPTCHA_UUID}`;
        }

        init();
        captchaImg.onclick = init;

        function getUUID() {
            if (crypto && crypto.randomUUID)
                return crypto.randomUUID();
            else return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                const r = (Math.random() * 16) | 0;
                const v = c === 'x' ? r : (r & 0x3) | 0x8;
                return v.toString(16);
            })
        }
    })();
</script>
```

我们可以把上述前端逻辑封装一下

### 保护控制器

在需要保护的接口上添加注解`@ImageCaptchaCheck`。

```java
@PostMapping("/create_user")
@ImageCaptchaCheck
boolean createUser(@ModelAttribute User user);
```

基本上这样就可以了，拦截器会自动校验验证码，ok 的话走下去业务逻辑，不 ok 抛出异常拦截。