# Sample Spring Boot App
That runs at AJ-Framework.


# Config File
Create YAML file `application.yaml` under `main/resources` folder.

```yml
server:
  port: 8081

spring:
  #  main:
  #    lazy-initialization: true
#  autoconfigure:
#    exclude:
#      - org.redisson.spring.starter.RedissonAutoConfigurationV2 # 禁用 Redis
  output:
    ansi:
      enabled: always
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true
    hibernate:
      ddl-auto: update
  redis:
    host: xxxx
    port: 123
    password: xxxxxxxxxxx
    ssl: true


management:
  endpoints:
    web:
      exposure:
        include: "*"
```