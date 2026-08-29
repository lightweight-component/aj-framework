---
title: Preventing Duplicate Submission
subTitle: 2024-12-05 by Frank Cheung
description: Preventing Duplicate Submission
date: 2022-01-05
tags:
  - last one
layout: layouts/docs.njk
---

# Preventing Duplicate Submission

Preventing duplicate submissions (such as form or API "double submissions" or "refresh duplicates") is a common
requirement in backend development. Common validation and protection solutions are as follows:

## Frontend Disable Button (Basic Solution)

When submitting a form, the frontend disables the button or displays a loading state to prevent users from clicking
multiple times. This can only prevent accidental operations, but cannot prevent malicious or rapid duplicate
submissions.

## Backend Idempotency Validation

Disabling the button on the frontend only prevents accidental actions; backend validation is safer and more reliable.

### Unique Token Validation

- When the frontend requests the form page, the backend generates a unique token (such as a UUID) and returns it to the
  frontend.
- The frontend includes the token when submitting the form.
- The backend checks whether the token has been used; if it has, the submission is rejected, and the token is
  immediately invalidated.
- Tokens can be stored using Redis, a database, etc.

### Uniqueness Validation Based on Request Content

- For the same user and same business parameters, submissions are only allowed once within a short period.
- You can use a hash of the request parameters (such as MD5), combined with the user ID, as the Redis key, which can
  only be used once within a short time.

## Reference

- [resubmit: Progressive Duplicate Submission Prevention Framework (Chinese)](https://mp.weixin.qq.com/s/tVkeyrDNc_scRusbClrY1w)