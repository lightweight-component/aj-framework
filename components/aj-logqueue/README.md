当系统流量负载比较高时，业务日志的写入操作也要纳入系统性能考量之内，如若处理不当，
将影响系统的正常业务操作，之前写过一篇《spring
boot通过MQ消费log4j2的日志》的博文，采用了RabbitMQ消息中间件来存储抗高并发下的日志，
因为引入了中间件，操作使用起来可能没那么简便，今天分享使用多线程消费阻塞队列的方式来处理我们的海量日志 



http://kailing.pub/article/index/arcid/153.html


参考博文如下，对BlockingQueue队列更多了解，可读一读如下的博文：

      http://blog.csdn.net/vernonzheng/article/details/8247564
      http://www.infoq.com/cn/articles/java-blocking-queue
      http://wsmajunfeng.iteye.com/blog/1629354
