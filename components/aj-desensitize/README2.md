#### 开源pom依赖引用
https://mingyang.blog.csdn.net/article/details/130324987

##### 新增JsonNullField注解，可将指定的字段值置为null，注解定义如下：

```java
/**
 *   自定义注解，标注在属性上，字段属性值置为null
 * ---------------------------------------------
 * 生效规则：
 * 1.非int、double、float、byte、short、long、boolean、char八种基本数据类型字段才会生效；
 * 2.
 * ---------------------------------------------
 * @author  Emily
 * @since :  Created in 2023/7/14 5:22 下午
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonNullField {

}
```

------

#### 解锁新技能《Java基于注解的脱敏实现组件SDK》

> 平时开发的过程中经常会遇到对一些敏感的字段进行脱敏处理，防止信息泄漏，如：邮箱、用户名、密码等；做为一个优秀的程序员我们不应该遇到这种问题时就做特殊处理，重复做相同的工作，所以我们应该写一个基础库SDK，解决重复的问题；

##### 一、定义注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSensitive {
}
```

> @JsonSensitive标注在类上，表示此类需要进行脱敏处理；

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSimField {
    /**
     * 脱敏类型，见枚举类型{@link SensitiveType}
     *
     * @return
     */
    SensitiveType value() default SensitiveType.DEFAULT;
}
```

> @JsonSimField标注在类的String、Collection<String>、String[]字段上，表示对这些字段值进行脱敏处理；

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFlexField {
    /**
     * 要隐藏的参数key名称
     *
     * @return
     */
    String[] fieldKeys() default {};

    /**
     * 要隐藏的参数值的key名称
     *
     * @return
     */
    String fieldValue();

    /**
     * 脱敏类型，见枚举类型{@link SensitiveType}
     *
     * @return
     */
    SensitiveType[] types() default {};
}
```

> @JsonFlexField注解标注在复杂数据类型字段上，具体的使用方法会在后面举例说明；



##### 三、基于注解的脱敏SDK使用案例

- 对实体类中字段为字符串类型脱敏处理

```java
@JsonSensitive
public class PubRequest {
    @JsonSimField(SensitiveType.USERNAME)
    public String username;
    @JsonSimField
    public String password;
    }
```

- 对实体类中字段是List<String>、Map<String,String>、String[]集合类型进行脱敏处理

```java
@JsonSensitive
public class PubRequest {
    @JsonSimField
    public Map<String, String> work;
    @JsonSimField
    public List<String> jobList;
    @JsonSimField
    public String[] jobs;
}
```

- 实体类中的字段是复杂数据类型脱敏处理

```java
@JsonSensitive
public class JsonRequest extends Animal{
    @JsonFlexField(fieldKeys = {"email", "phone"}, fieldValue = "fieldValue", types = {SensitiveType.EMAIL, SensitiveType.PHONE})
    private String fieldKey;
    private String fieldValue;
}
```

> 复杂数据类型其实就是fieldKey可以指定多个不同的字段名，fieldValue是具体的字段值，如果fieldKey是email时fieldValue传递的就是邮箱，就按照types中指定脱敏策略为邮箱的策略脱敏；

- 实体类中的属性字段是集合类型，集合中存放的是嵌套的实体类

```java
    @JsonSensitive
    public static class Job {
        @JsonSimField(SensitiveType.DEFAULT)
        private String work;
        @JsonSimField(SensitiveType.EMAIL)
        private String email;
    }
```

嵌套实体类属性字段

```java
    public Job job;
    public Map<String, Object> work;
    public List<PubResponse.Job> jobList;
    public PubResponse.Job[] jobs;
```

>
如果实体类中的集合中存放的是实体类，并且这个实体类标注了@JsonSensitive注解，则会对嵌套实体类中标注了@JsonSimField、@JsonFlexField注解的字段进行脱敏处理；同样如果最外层是集合、数组、key-value类型则也会对内部嵌套的实体类进行脱敏处理；

本文只对脱敏SDK做大概的阐述，如果你需要源码可以到个人GitHub上去拉；本文的示例是对当前实体类对象本身进行脱敏处理，返回的还是原来的对象本身，个人GitHub示例中还有一个返回是非当前对象的SDK工具类SensitiveUtils；

GitHub地址：[https://github.com/mingyang66/spring-parent](https://github.com/mingyang66/spring-parent)