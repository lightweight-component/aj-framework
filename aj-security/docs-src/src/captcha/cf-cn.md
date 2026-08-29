---
title: CloudFlare Turnstile Captcha
subTitle: 2024-12-05 by Frank Cheung
description: CloudFlare Turnstile Captcha
date: 2022-01-05
tags:
  - CloudFlare
layout: layouts/docs-cn.njk
---

# CloudFlare Turnstile Captcha

我们封装了 CloudFlare Turnstile Captcha 的封装，方便使用。

TODO

如果是标准 Form 表单提交，请不要声明`enctype="application/x-www-form-urlencoded"`，因为这样会导致 CloudFlare Turnstile
反馈`missing-input-response`的错误。