---
title: Google Recaptcha
subTitle: 2024-12-05 by Frank Cheung
description: Google Recaptcha
date: 2022-01-05
tags:
  - Google Recaptcha
layout: layouts/docs-cn.njk
---

# Google Recaptcha

我们封装了 Google Recaptcha 验证，方便使用。reCAPTCHA v3 很牛逼，取消了用户互动交互，做到无感知验证。

## 准备

要有谷歌账户，去注册 [https://www.google.com/recaptcha/admin](https://www.google.com/recaptcha/admin)。我们使用 reCAPTCHA
v3

![](/captcha/413153c9f85df5e54d02b1401f3da4de.png)

填入你的域名，技巧：
1、可多个域名存放在一个网站下（其实实际是不同网站也没关系）

2、添加 `localhost` 便于本地开发测试

最后获取 appId 和密钥。

![](/captcha/1.jpg)

# 使用

我们使用自定义用法。

## 引入脚本

```html
<!-- Google 防注册机验证 -->
<style>.grecaptcha-badge{display: none;}</style> 
<script src="https://www.recaptcha.net/recaptcha/api.js?render=XXXX"></script>
```

`render` 参数是你的客户 appId，注意不是密钥。另外一个样式是隐藏 Google 标签的，自然大多数客户不想看到。

![](/captcha/164f43cd827274a704fa4002b62dc529.png)

## 前端

一般来说要写入操作的表单都要验证一下（写入的操作）。我们在表单之前获取 Token 并作为参数传到后端。下面是标准表单提交的例子。

```html
<script src="https://www.recaptcha.net/recaptcha/api.js?render=6LclfLMZAAAAAKC3YUTP4E3Ylc0PSvfXpneRePAH"></script>

<form id="myForm" action="http://localhost:8083/foo/captcha_google" method="POST">
    <input type="text" name="name" />
    <br />
    <input type="text" name="age" />
    <br />
    <input type="hidden" name="grecaptchaToken" id="recaptchaToken" />
    <button type="submit">Submit</button>
    <p id="status"></p>
</form>

<script>
    document.getElementById('myForm').addEventListener('submit', function (e) {
    debugger
      e.preventDefault(); // Prevent default form submission
    
      const form = e.target;
      const tokenInput = document.getElementById('recaptchaToken');
    
      grecaptcha.ready(function () {
        grecaptcha.execute('6LclfLMZAAAAAKC3YUTP4E3Ylc0PSvfnp8eRePAH', { action: 'submit' }).then(function (token) {
        debugger
          tokenInput.value = token; // Set token value in hidden input
          form.submit(); // Now submit the form
        });
      });
    });

</script>
```

相应修改 appId 即可。

POST JSON Raw Body 的例子：

```javascript
document.getElementById('myForm').addEventListener('submit', function (e) {
  e.preventDefault();

  const form = e.target;
  const status = document.getElementById('status');

  // Execute reCAPTCHA
  grecaptcha.ready(function () {
    grecaptcha.execute('6LclfLMZAAAAAKC3YUTP4Ylc0PSvfnpneRePAH', { action: 'submit' }).then(function (token) {
      // Add token to form data
      const formData = new FormData(form);
      formData.append('token', token);

      // Submit form via fetch
      fetch('/api/submit-form', {
        method: 'POST',
        body: JSON.stringify(Object.fromEntries(formData)),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(response => response.json()).then(data => {
        status.textContent = data.message || 'Success!';
        status.style.color = 'green';
        form.reset();
      }).catch(error => {
        status.textContent = 'An error occurred.';
        status.style.color = 'red';
        console.error('Error:', error);
      });
    });
  });
});

```

## 服务端处理

后端的原理就是把传入的 token 再请求 Google 校验是否合法。

### yaml 配置

配置文件 `application.yml` 中添加如下内容：

```yaml
security:
  captcha:
    google:
      enabled: true
      globalCheck: false # 全局检查
      accessSecret: 6LclfLMZAAAAAD6XUpBL0qHKYWijay7-lGpOf
```

### 拦截校验

在使用的接口上添加`@GoogleCaptchaCheck`注解：

```java
@PostMapping("/captcha_google")
@GoogleCaptchaCheck
boolean google(@RequestParam(GoogleCaptcha.PARAM_NAME) String token, @ModelAttribute User user);
```

校验通过，结果如下:

![](/captcha/d20ef7798177009e31b7125122736d5f.png)