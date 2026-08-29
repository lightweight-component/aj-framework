---
title: Google Recaptcha
subTitle: 2024-12-05 by Frank Cheung
description: Google Recaptcha
date: 2022-01-05
tags:
  - Google Recaptcha
layout: layouts/docs.njk
---

# Google reCAPTCHA

We have encapsulated the Google reCAPTCHA verification for easier use. reCAPTCHA v3 is powerful and eliminates user
interaction, enabling seamless and invisible verification.

## Preparation

You need a Google account to register
at [https://www.google.com/recaptcha/admin](https://www.google.com/recaptcha/admin). We will be using **reCAPTCHA v3**.

![](/captcha/413153c9f85df5e54d02b1401f3da4de.png)

Fill in your domain name. Tips:

1. You can place multiple domains under one site (in reality, even different sites are fine).
2. Add `localhost` to make local development and testing easier.

Finally, obtain your **site key (appId)** and **secret key**.

![](/captcha/1.jpg)

# Usage

We use a custom implementation.

## Include the Script

```html
<!-- Google anti-bot verification -->
<style>.grecaptcha-badge{display: none;}</style> 
<script src="https://www.recaptcha.net/recaptcha/api.js?render=XXXX"></script>
```

The `render` parameter is your **site key**, not the secret key. The included CSS style hides the Google badge — most
users won’t want to see it.

![](/captcha/164f43cd827274a704fa4002b62dc529.png)

## Frontend

Usually, any form that triggers a write operation should include reCAPTCHA validation. We fetch the token before
submitting the form and pass it to the backend as a parameter. Below is an example of a standard form submission.

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

Update the `site key` accordingly.

### Example Using POST JSON Raw Body

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

## Backend Handling

The backend logic involves sending the
received [token](file://d:\sp42\code\zhongen-rdd\ui\src\api\user\index.ts#L29-L29) to Google for verification.

### YAML Configuration

Add the following configuration to your `application.yml` file:

```yaml
security:
  captcha:
    google:
      enabled: true
      globalCheck: false # Global check
      accessSecret: 6LclfLMZAAAAAD6XUpBL0qHKYWijay7-lGpOf
```

### Intercept and Validate

Add the `@GoogleCaptchaCheck` annotation to the relevant interface:

```java
@PostMapping("/captcha_google")
@GoogleCaptchaCheck
boolean google(@RequestParam(GoogleCaptcha.PARAM_NAME) String token, @ModelAttribute User user);
```

Once verified successfully, you'll get a result like this:

![](/captcha/d20ef7798177009e31b7125122736d5f.png)

