https://cloud.tencent.com/developer/article/1718134
https://www.51cto.com/article/775541.html
https://cloud.tencent.com/developer/article/1785733
https://cloud.tencent.com/developer/article/1776554
https://cloud.tencent.com/developer/article/2195562


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