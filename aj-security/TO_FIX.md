# Security module to-fix list

This document records findings from a read-only review of the Java source code.

## High priority

### ParamsSign does not sign the nonce and timestamp

`ParamsSign.sign()` copies existing values into `paramMap` before adding `nonce` and `timestamp` to the original map. Consequently, the HMAC does not bind either anti-replay value. An attacker can alter the timestamp to bypass expiry checks or replace the nonce to evade a used-nonce record.

- Evidence: `src/main/java/com/ajaxjs/security/paramssign/ParamsSign.java:66`
- Fix: add `nonce` and `timestamp` before building the canonical parameter map, and sign every security-relevant request field.

### HTTP Digest authentication accepts arbitrary and replayed nonces

The server generates a nonce but does not persist, validate, expire, or consume it. It also hashes the URI declared in the Authorization header without comparing it with the actual request URI. Captured credentials can be replayed and may be usable against another endpoint.

- Evidence: `src/main/java/com/ajaxjs/security/httpauth/HttpDigestAuth.java:63`
- Fix: issue signed or server-stored expiring nonces; validate nonce count and URI against the request; reject stale or reused values; compare digest values in constant time.

### SimpleLimit permanently exhausts permits

`SimpleLimit` acquires a semaphore permit, but its release is commented out. `SecurityInterceptor.afterCompletion()` also does not invoke the stored cleanup callback. Once all permits are acquired, later requests are permanently rejected.

- Evidence: `src/main/java/com/ajaxjs/security/limit/simplelimit/SimpleLimit.java:55`
- Evidence: `src/main/java/com/ajaxjs/security/SecurityInterceptor.java:176`
- Fix: invoke the after-completion callback and release exactly one permit for every successful acquisition, including exceptional request completion.

### IP filtering trusts spoofable forwarding headers and has unbounded state

`IpUtils` prioritizes `X-Forwarded-For` without a trusted-proxy policy. An attacker can submit a victim address, trigger the suspicious-request path, and permanently blacklist that victim. The blacklist and per-IP rate-limit map have neither expiration nor size limits.

- Evidence: `src/main/java/com/ajaxjs/security/iplist/IpUtils.java:54`
- Evidence: `src/main/java/com/ajaxjs/security/iplist/IpSecurityFilter.java:22`
- Fix: only honor forwarding headers from configured trusted proxies; use bounded, expiring caches; do not permanently blacklist on simple heuristic matches.

## Incomplete or disconnected features

### LimitAccess denies every request

`LimitAccess.action()` unconditionally returns `false`. If registered as an interceptor action, every annotated endpoint is denied.

- Evidence: `src/main/java/com/ajaxjs/security/limit/LimitAccess.java:20`
- Fix: implement the configured limit check or remove the feature until it is complete.

### Parameter-signature interceptor is disabled

The `ParamsSignAction` invocation is commented out in the shared interceptor.

- Evidence: `src/main/java/com/ajaxjs/security/SecurityInterceptor.java:95`
- Fix: restore it only after correcting the signature/replay defect above and defining failure responses.

### Audit logging uses Referer configuration and is not wired in

Audit logging is configured with the HTTP Referer property key/prefix, has no call from `SecurityInterceptor`, and leaves request argument binding and asynchronous persistence as TODOs.

- Evidence: `src/main/java/com/ajaxjs/security/auditlog/AuditOperationLogAction.java:25`
- Fix: give it a dedicated configuration namespace, register it in the interceptor chain, and implement safe asynchronous persistence.

### Deprecated implementations remain fully commented out

The Redis limiter, encrypted-body converter, and old captcha servlet are retained as commented source rather than maintained code or deleted code.

- Fix: either remove obsolete source or restore it as tested, compilable modules.

### Non-repeat submission is incomplete and incorrectly scoped

Cookie token extraction is unimplemented. In AUTO mode, `userId` is obtained but omitted from the hash, so distinct users can block each other on the same URI. Unconfigured callback functions can cause null-pointer failures.

- Evidence: `src/main/java/com/ajaxjs/security/nonrepeatsubmit/NonRepeatSubmitMgr.java:60`
- Evidence: `src/main/java/com/ajaxjs/security/nonrepeatsubmit/NonRepeatSubmitMgr.java:88`
- Fix: define mandatory storage dependencies, include a stable user/tenant identifier and relevant request identity in the idempotency key, and implement the supported token transports.

## Medium priority

### IP rate limiting is not thread safe

The `RateLimitInfo` token count and reset path are mutated without synchronization or atomic operations, so concurrent requests can bypass or over-trigger the limit.

- Evidence: `src/main/java/com/ajaxjs/security/iplist/IpSecurityFilter.java:80`
- Fix: use atomic state transitions, synchronization, or a mature bounded rate-limiter implementation.

### HTTP Basic authentication mishandles malformed credentials

Malformed Base64 data or credentials without `:` can trigger an exception and return 500 instead of 401. Plain `String.equals` is not a constant-time secret comparison.

- Evidence: `src/main/java/com/ajaxjs/security/httpauth/HttpBasicAuth.java:53`
- Fix: validate decode and split results, always return a generic 401 challenge on malformed input, and use a constant-time byte comparison for secret values.

### Cloudflare validation can block request threads indefinitely

The fallback `RestTemplate` has no explicit connection/read timeout, and the code catches `Throwable`, including fatal JVM errors.

- Evidence: `src/main/java/com/ajaxjs/security/captcha/cloudflare/Cloudflare.java:60`
- Evidence: `src/main/java/com/ajaxjs/security/captcha/cloudflare/Cloudflare.java:94`
- Fix: inject a configured HTTP client with bounded timeouts, retries/circuit breaking where appropriate, and catch expected transport exceptions only.

### TokenBucket accepts invalid capacity and rate values

Zero refill rate can cause division by zero; negative capacity/rate values give undefined limiter behavior.

- Evidence: `src/main/java/com/ajaxjs/security/ratelimit/TokenBucket.java:126`
- Fix: reject non-positive capacity and refill rate at construction and dynamic update boundaries.

### Blocking bandwidth limiting consumes Servlet threads

The limiter parks the request thread while waiting for tokens. Long responses or malicious traffic can reduce servlet pool availability.

- Fix: document this constraint or move bandwidth control to asynchronous I/O, a proxy, or a dedicated traffic-control layer.

### Configuration and failure handling are inconsistent

Security property names use inconsistent casing, several components rely on static context access through `DiContextUtil`, and failure paths mix exceptions with boolean return values.

- Fix: normalize property namespaces, inject request/response dependencies conventionally, and define a consistent 401/403/429 error contract.

## Recommended repair order

1. Correct ParamsSign, Digest authentication, SimpleLimit cleanup, and forwarding-header trust.
2. Complete or remove disconnected and commented-out features.
3. Introduce consistent security failure responses and validated configuration.
4. Add regression tests for signature coverage, nonce replay, Digest URI binding, semaphore release, IP spoofing, and concurrent rate limiting.
