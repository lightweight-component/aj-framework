package com.ajaxjs.security.antireptile;

import com.ajaxjs.security.antireptile.config.AntiReptileProperties;
import com.ajaxjs.security.antireptile.model.VerifyImageDTO;
import com.ajaxjs.security.antireptile.rule.RuleActuator;
import com.ajaxjs.security.antireptile.util.CrosUtil;
import com.ajaxjs.security.antireptile.util.VerifyImageUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Spring MVC 反爬拦截器，连接受保护接口、规则执行器与验证码页面。
 * <p>首次处理请求时读取验证页面模板及 Spring Bean；随后根据全局模式、方法注解
 * 和 URI 配置决定是否执行规则。任一规则命中时，生成并缓存验证码，
 * 以 HTTP 509 返回填充后的 HTML 页面，并终止当前控制器调用。非控制器方法处理器直接放行。</p>
 */
@Slf4j
public class AntiReptileInterceptor implements HandlerInterceptor /*HandlerInterceptorAdapter*/ {
    /**
     * 从类路径 verify/index.html 加载的 HTML 模板，包含验证码和原始 URI 占位符。
     */
    private String antiReptileForm;

    /**
     * 按优先级执行全部反爬规则的执行器。
     */
    private RuleActuator actuator;

    /**
     * 需要检查的 URI 配置，既参与精确匹配，也作为完整匹配的正则表达式使用。
     */
    private List<String> includeUrls;

    /**
     * 是否对全部控制器方法执行规则检查。
     */
    private boolean globalFilterMode;

    /**
     * 生成图片并保存验证码答案的工具。
     */
    private VerifyImageUtil verifyImageUtil;

    /**
     * 是否已完成首次请求触发的初始化。
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 加载 UTF-8 验证页面模板，并从 Web 应用上下文获取依赖与配置快照。
     * <p>模板读取失败时记录警告并继续初始化；未配置 URI 列表时使用空列表。
     * 初始化标记由调用方设置，本方法自身不提供并发初始化互斥。</p>
     *
     * @param context 当前 Web 应用的 Servlet 上下文，须能找到 Spring 上下文
     */
    public void init(ServletContext context) {
        ClassPathResource classPathResource = new ClassPathResource("verify/index.html");

        try {
            classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            this.antiReptileForm = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("反爬虫验证模板加载失败！", e);
        }

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        assert ctx != null;
        this.actuator = ctx.getBean(RuleActuator.class);
        this.verifyImageUtil = ctx.getBean(VerifyImageUtil.class);
        this.includeUrls = ctx.getBean(AntiReptileProperties.class).getIncludeUrls();
        this.globalFilterMode = ctx.getBean(AntiReptileProperties.class).isGlobalFilterMode();

        if (this.includeUrls == null)
            this.includeUrls = new ArrayList<>();
    }

    /**
     * 在控制器调用前按接口范围执行反爬检查。
     * <p>规则命中时缓存新验证码，将标识、图片和原始 URI 替换到模板，
     * 写入 HTTP 509 HTML 响应并关闭输出流；其他情况继续请求处理。</p>
     *
     * @param request  当前 HTTP 请求
     * @param response 用于返回验证页面的 HTTP 响应
     * @param handler  Spring MVC 选中的处理器，非 HandlerMethod 类型直接放行
     * @return 允许继续处理时为 {@code true}；已返回验证页面时为 {@code false}
     * @throws Exception 初始化、规则执行、验证码存储或响应写入失败时向外传播的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!initialized.get()) {
            init(request.getServletContext());
            initialized.set(true);
        }

        HandlerMethod handlerMethod;

        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (ClassCastException e) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        AntiReptile antiReptile = AnnotationUtils.findAnnotation(method, AntiReptile.class);
        boolean isAntiReptileAnnotation = antiReptile != null;
        String requestUrl = request.getRequestURI();

        if (isIntercept(requestUrl, isAntiReptileAnnotation) && !actuator.isAllowed(request, response)) {
            CrosUtil.setCrosHeader(response);
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(509);
            VerifyImageDTO verifyImage = verifyImageUtil.generateVerifyImg();
            verifyImageUtil.saveVerifyCode(verifyImage);
            String str1 = this.antiReptileForm.replace("verifyId_value", verifyImage.getVerifyId());
            String str2 = str1.replaceAll("verifyImg_value", verifyImage.getVerifyImgStr());
            String str3 = str2.replaceAll("realRequestUri_value", requestUrl);
            response.getWriter().write(str3);
            response.getWriter().close();

            return false;
        }

        return true;
    }

    /**
     * 判断指定 URI 是否需要进入规则检查。
     * <p>全局模式、方法注解、URI 列表精确匹配满足任一条件即返回真；
     * 否则逐项使用 Java 正则表达式对整个 URI 匹配，不采用 Ant 路径匹配语法。</p>
     *
     * @param requestUrl              待检查的请求 URI，不包含查询字符串
     * @param isAntiReptileAnnotation 方法是否带反爬注解，不能为 {@code null}
     * @return 需要执行规则检查时为 {@code true}，不表示请求已经命中规则
     * @throws java.util.regex.PatternSyntaxException 遍历到的 URI 配置不是合法正则表达式时抛出
     */
    public boolean isIntercept(String requestUrl, Boolean isAntiReptileAnnotation) {
        if (this.globalFilterMode || isAntiReptileAnnotation || this.includeUrls.contains(requestUrl))
            return true;
        else {
            for (String includeUrl : includeUrls) {
                if (Pattern.matches(includeUrl, requestUrl))
                    return true;
            }

            return false;
        }
    }
}
