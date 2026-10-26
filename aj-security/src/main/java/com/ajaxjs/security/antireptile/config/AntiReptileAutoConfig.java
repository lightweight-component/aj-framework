package com.ajaxjs.security.antireptile.config;

import com.ajaxjs.security.antireptile.AntiReptileInterceptor;
import com.ajaxjs.security.antireptile.rule.AntiReptileRule;
import com.ajaxjs.security.antireptile.rule.IpRuleHandler;
import com.ajaxjs.security.antireptile.rule.RuleActuator;
import com.ajaxjs.security.antireptile.rule.UaRuleHandler;
import com.ajaxjs.security.antireptile.servlet.RefreshFormServlet;
import com.ajaxjs.security.antireptile.servlet.ValidateFormService;
import com.ajaxjs.security.antireptile.servlet.ValidateFormServlet;
import com.ajaxjs.security.antireptile.util.LocalCache;
import com.ajaxjs.security.antireptile.util.VerifyImageUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 反爬 Spring Boot Starter 的组件装配入口。
 * <p>仅在 {@code anti.reptile.manager.enabled=true} 时生效，导入本地缓存与 MVC 配置，
 * 注册验证码 Servlet、工具、服务及拦截器。内置 IP 和 User-Agent 规则默认注册，
 * 可分别通过规则的 {@code enabled} 配置关闭；自定义规则 Bean 也会参与执行。</p>
 */
@Configuration
@EnableConfigurationProperties(AntiReptileProperties.class)
@ConditionalOnProperty(prefix = "anti.reptile.manager", value = "enabled", havingValue = "true")
@Import(WebMvcConfig.class)
public class AntiReptileAutoConfig {
    /**
     * 创建组件私有的单机过期缓存，不与宿主其他缓存共享条目。
     * @param properties 缓存容量配置
     * @return 存储请求计数、IP 锁定和验证码答案的缓存
     */
    @Bean(destroyMethod = "clear")
    public LocalCache antiReptileLocalCache(AntiReptileProperties properties) {
        return new LocalCache(properties.getLocalCacheCapacity());
    }

    /**
     * 提交验证码答案的 Servlet 路径。
     */
    public static final String VALIDATE_REQUEST_URI = "/kk-anti-reptile/validate";

    /**
     * 注册提交验证码答案的 Servlet，并启用容器标准 multipart 解析。
     * <p>该 Servlet 不经过 DispatcherServlet，需要独立设置 multipart 配置，
     * 才能通过请求参数接口读取前端 FormData 字段；使用容器默认临时目录，不额外限制请求大小。</p>
     *
     * @return 映射到 {@link AntiReptileAutoConfig#VALIDATE_REQUEST_URI} 的 Servlet 注册信息
     */
    @Bean
    public ServletRegistrationBean<ValidateFormServlet> validateFormServlet() {
        ServletRegistrationBean<ValidateFormServlet> registration = new ServletRegistrationBean<>(new ValidateFormServlet(), VALIDATE_REQUEST_URI);
        registration.setMultipartConfig(new MultipartConfigElement(""));

        return registration;
    }

    /**
     * 刷新验证码的 Servlet 路径。
     */
    public static final String REFRESH_REQUEST_URI = "/kk-anti-reptile/refresh";

    /**
     * 注册刷新验证码的 Servlet。
     *
     * @return 映射到 {@link AntiReptileAutoConfig#REFRESH_REQUEST_URI} 的 Servlet 注册信息
     */
    @Bean
    public ServletRegistrationBean<RefreshFormServlet> refreshFormServlet() {
        return new ServletRegistrationBean<>(new RefreshFormServlet(), REFRESH_REQUEST_URI);
    }

    /**
     * 在 IP 规则开关为真或未配置时创建默认 IP 处理器。
     *
     * @return 基于本地缓存计数的 IP 规则处理器
     */
    @Bean
    @ConditionalOnProperty(prefix = "anti.reptile.manager.ip-rule", value = "enabled", havingValue = "true", matchIfMissing = true)
    public IpRuleHandler ipRule() {
        return new IpRuleHandler();
    }

    /**
     * 在 User-Agent 规则开关为真或未配置时创建默认处理器。
     *
     * @return 根据设备与操作系统识别结果检查请求的规则处理器
     */
    @Bean
    @ConditionalOnProperty(prefix = "anti.reptile.manager.ua-rule", value = "enabled", havingValue = "true", matchIfMissing = true)
    public UaRuleHandler uaRule() {
        return new UaRuleHandler();
    }

    /**
     * 创建默认 kaptcha 生成器，宿主应用可提供自己的 Producer Bean 覆盖此配置。
     * <p>默认生成 130×48 像素的五位静态文字验证码，字体大小为 32，
     * 图片由验证码工具编码为 PNG，不使用 kaptcha 的 Session 存储机制。</p>
     *
     * @return 为默认图片尺寸和文字长度配置的验证码生成器
     */
    @Bean
    @ConditionalOnMissingBean(Producer.class)
    public Producer captchaProducer() {
        Properties properties = new Properties();
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "130");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "48");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "5");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "32");
        DefaultKaptcha producer = new DefaultKaptcha();
        producer.setConfig(new Config(properties));

        return producer;
    }

    /**
     * 创建验证码生成和答案缓存工具。
     *
     * @return 由 Spring 注入本地缓存的验证码工具
     */
    @Bean
    public VerifyImageUtil verifyImageUtil() {
        return new VerifyImageUtil();
    }

    /**
     * 收集全部规则并按优先级数值升序创建执行器。
     *
     * @param rules 容器中的全部反爬规则 Bean，包含用户自定义实现
     * @return 按优先级顺序检查请求的规则执行器
     */
    @Bean
    public RuleActuator ruleActuator(final List<AntiReptileRule> rules) {
        List<AntiReptileRule> antiReptileRules = rules.stream().sorted(Comparator.comparingInt(AntiReptileRule::getOrder)).collect(Collectors.toList());

        return new RuleActuator(antiReptileRules);
    }

    /**
     * 创建验证码校验与刷新业务服务。
     *
     * @return 由 Spring 注入规则执行器和验证码工具的服务
     */
    @Bean
    public ValidateFormService validateFormService() {
        return new ValidateFormService();
    }

    /**
     * 创建 MVC 反爬拦截器。
     *
     * @return 首次请求时初始化依赖和页面模板的拦截器
     */
    @Bean
    public AntiReptileInterceptor antiReptileInterceptor() {
        return new AntiReptileInterceptor();
    }
}
