---
name: aj-beanvalidator
description: "Use AJ Bean Validator, a lightweight Spring MVC validation starter, when adding, configuring, troubleshooting, or extending validation in a Spring Boot 2.7+ application without Hibernate Validator."
---

# AJ Bean Validator

Use this skill for projects that depend on `com.ajaxjs:beanvalidator`. It is a lightweight Spring MVC validation starter; it does not implement Jakarta/Javax Bean Validation and does not require Hibernate Validator.

## Compatibility and setup

- The library JAR targets Java 8 bytecode. Its supported baseline is Java 8+ and Spring Boot 2.7+.
- Spring Boot 2.7 applications can run on Java 8+. Boot 3 and Boot 4 applications themselves require Java 17+.
- Add the `com.ajaxjs:beanvalidator` dependency. In Servlet-based Spring MVC, its auto-configuration installs the object validator and path-variable resolver automatically. Do not manually instantiate `ValidatorInitializing` in an ordinary Boot application.
- Disable the integration, when needed, with `ajaxjs.beanvalidator.enabled: false`.
- Do not add `javax.validation`, `jakarta.validation`, or Hibernate Validator merely to use this library's annotations. Import every constraint from `com.ajaxjs.framework.validator.custom`.

## Choose the right constraint

Use the library's own common constraints:

- `@NotNull` for any non-null value.
- `@NotBlank` for `CharSequence` values that must contain non-whitespace text.
- `@Size` for `CharSequence`, `Collection`, `Map`, or arrays.
- `@Min` and `@Max` for `Number` values.
- `@Pattern` for `CharSequence`; use its own `Pattern.Flag` enum.
- `@Email` for `CharSequence`; it also accepts `regexp` and `flags` for an extra restriction.

Use the business constraints for `@IdCard`, `@MobileNo`, `@Username`, `@Password`, `@Chinese`, `@Ipv4`, and `@HttpUrl`. Their `required` attribute defaults to `true`. With `required = false`, null, empty, and whitespace-only values skip the format check; non-empty values are always checked. Use `@NotNull` or `@NotBlank` when a value must be present.

Avoid treating these annotations as drop-in, complete Bean Validation equivalents. The supported value types and semantics above are the contract.

## Request and error handling

- On a request-body DTO, use Spring's `@Validated` together with `@RequestBody`. Constraint failures are collected in `BindingResult` / `Errors` and normally surface as `MethodArgumentNotValidException`.
- Supported annotations on `@PathVariable` are validated automatically. A failure throws `ValidatorException`; map it to HTTP 400 in the application's exception handler.
- A `ValidatorConfigurationException` means an application-development configuration problem, such as an unsupported library-package annotation or an absent/blank message. Do not present it as an ordinary client validation error.
- Message keys such as `{id-card}` resolve through `ajaxjs.beanvalidator.messages`. An explicit non-key message on an annotation has priority.

## Extend validation

For application-specific constraints, declare an annotation in the application package and provide a Spring bean implementing `ValidatorRule`. `supports` must identify only the intended annotation; `validate` must throw `ValidatorException` when the value fails. This rule is applied to DTO fields and path variables.

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantCode {
    String message() default "租户编码不正确";
}

@Component
class TenantCodeRule implements ValidatorRule {
    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType() == TenantCode.class;
    }

    @Override
    public void validate(Annotation annotation, Object value, String fieldName) {
        TenantCode constraint = (TenantCode) annotation;
        if (!(value instanceof String) || !((String) value).matches("TENANT-\\d+"))
            throw new ValidatorException(fieldName + " " + constraint.message());
    }
}
```

Do not place application-specific annotations under `com.ajaxjs.framework.validator.custom`: that package is reserved for built-in constraints, and an unknown annotation there is treated as a configuration error.

## When changing the library

- Preserve Java 8 source compatibility and the Spring Boot 2.7 API baseline.
- Keep the starter lightweight: do not introduce Hibernate Validator or standard `javax`/`jakarta` constraint dependencies without an explicit requirement.
- Run `mvn -pl aj-beanvalidator test` after code changes. Run `mvn -pl aj-beanvalidator javadoc:javadoc` after documentation changes; fix all reported errors and warnings.
