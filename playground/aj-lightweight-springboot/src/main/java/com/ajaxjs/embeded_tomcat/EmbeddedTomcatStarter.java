package com.ajaxjs.embeded_tomcat;

import com.ajaxjs.embeded_tomcat.filter.FileUploadHelper;
import com.ajaxjs.embeded_tomcat.filter.UTF8CharsetFilter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleState;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.util.Map;

/**
 * 嵌入式使用 Tomcat
 */
public class EmbeddedTomcatStarter extends TomcatStarter {
    /**
     * LifecycleState.STARTING_PREP 会执行两次，不知为何
     */
    public boolean isStatedSpring;

    /**
     * 配置类
     */
    Class<?>[] clz;

    public EmbeddedTomcatStarter(TomcatConfig cfg, Class<?>[] clz) {
        super(cfg);
        this.clz = clz;
    }

    /**
     * 当上下文准备就绪时触发的回调方法。
     * 主要用于初始化 Spring Web 应用程序上下文及相关配置。
     *
     * @param context Servlet 上下文，用于访问和配置 Servlet 容器的相关功能
     */
    @Override
    public void onContextReady(Context context) {
        context.addLifecycleListener((LifecycleEvent event) -> { // 添加生命周期监听器，以在特定生命周期事件发生时执行逻辑
            // 仅在 Spring 尚未初始化或当前生命周期状态不是 STARTING_PREP时 执行以下操作。
            if (isStatedSpring || (event.getLifecycle().getState() != LifecycleState.STARTING_PREP))
                return;

            ServletContext ctx = context.getServletContext();

            if (ctx == null) // 如果ServletContext为空，可能是处于测试环境，直接返回
                return;

            // 创建 AnnotationConfigWebApplicationContext 实例，用于配置和管理 Spring Web 上下文。
            // 通过注解的方式初始化 Spring 的上下文，注册 Spring 的配置类（替代传统项目中 xml 的 configuration）
            AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
            ac.setServletContext(ctx);

            if (!ObjectUtils.isEmpty(clz))// 如果有指定的配置类，则注册这些配置类
                ac.register(clz);

            ac.refresh(); // 刷新上下文，使配置生效
            ac.registerShutdownHook(); // 注册关闭钩子，确保在应用关闭时能够正确处理 Spring 上下文的关闭

            // 配置 ServletContext 参数，指定使用的上下文类。
            ctx.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
            ctx.addListener(new ContextLoaderListener()); // 添加 ContextLoaderListener 监听器，用于初始化和销毁 Spring Web 上下文
            ctx.setAttribute("ctx", ctx.getContextPath()); // 设置上下文路径到 ServletContext 属性，以便在 JSP 中使用

            // 绑定 Servlet，配置 Spring MVC 的 DispatcherServlet，并设置其加载优先级
            ServletRegistration.Dynamic registration = ctx.addServlet("dispatcher", new DispatcherServlet(ac));
            registration.setLoadOnStartup(1);// 设置 Tomcat 启动立即加载 Servlet
            registration.addMapping("/"); // 浏览器访问 uri。注意不要设置 /*

            // 配置字符过滤器，确保请求和响应的字符编码正确
            FilterRegistration.Dynamic filterReg = ctx.addFilter("InitMvcRequest", new UTF8CharsetFilter());
            filterReg.addMappingForUrlPatterns(null, true, "/*");

            if (cfg.getEnableLocalFileUpload()) { // 根据配置，初始化文件上传功能
                if (cfg.getLocalFileUploadDir() == null)
                    FileUploadHelper.initUpload(ctx, registration);
                else
                    FileUploadHelper.initUpload(ctx, registration, cfg.getLocalFileUploadDir());
            }

            if (cfg.isEnableJMX()) // 根据配置，启用 JMX 连接
                connectMBeanServer();

            isStatedSpring = true;// 标记 Spring 初始化完成，并记录初始化时间
            springTime = System.currentTimeMillis() - startedTime;
        });
    }

    /**
     * 启动嵌入式 Tomcat 服务器。
     * <p>
     * 此方法根据提供的参数初始化并启动一个嵌入式 Tomcat 服务器，支持自定义服务器端口、上下文路径以及本地文件上传配置。
     *
     * @param clz 由 Servlet 容器加载的类，通常是 Spring Boot 的启动类
     */
    public static void start(Class<?>... clz) {
        TomcatConfig cfg = new TomcatConfig();
        Map<String, Object> serverConfig = getServerConfig();
        int port = 8301; // default port

        if (serverConfig != null) {
            Object p = serverConfig.get("port"); // 尝试获取并设置自定义端口

            if (p != null)
                port = (int) p;

            String context = (String) serverConfig.get("context-path"); // 尝试获取并设置自定义上下文路径

            if (StringUtils.hasText(context))
                cfg.setContextPath(context);

            Object upObj = serverConfig.get("localFileUpload");  // 尝试获取并配置本地文件上传设置

            if (upObj != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> upCfg = (Map<String, Object>) upObj;

                if (upCfg.get("enable") != null)
                    cfg.setEnableLocalFileUpload((boolean) upCfg.get("enable"));

                cfg.setLocalFileUploadDir((String) upCfg.get("dir"));
            }
        }

        cfg.setPort(port); // 设置最终的端口配置

        new EmbeddedTomcatStarter(cfg, clz).start();  // 使用配置好的 Tomcat 配置和类启动嵌入式 Tomcat 服务器
    }

    /**
     * 从类路径下的 application.yml 文件中获取服务器配置。
     *
     * @return 服务器配置的 Map 对象，如果文件不存在或读取失败，则返回null
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> getServerConfig() {
        ClassPathResource resource = new ClassPathResource("application.yml");

        if (!resource.exists())
            return null;

        try {
            Map<String, Object> yamlMap = new Yaml().load(resource.getInputStream());

            return (Map<String, Object>) yamlMap.get("server");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动 Web 程序
     *
     * @param port 端口
     * @param clz  配置类列表
     */
    public static void start(int port, Class<?>... clz) {
        TomcatConfig cfg = new TomcatConfig();
        cfg.setPort(port);

        new EmbeddedTomcatStarter(cfg, clz).start();
    }

    /**
     * 启动 Web 程序
     *
     * @param port        端口
     * @param contextPath 程序上下文目录
     * @param clz         配置类列表
     */
    public static void start(int port, String contextPath, Class<?>... clz) {
        TomcatConfig cfg = new TomcatConfig();
        cfg.setPort(port);
        cfg.setContextPath(contextPath);

        new EmbeddedTomcatStarter(cfg, clz).start();
    }
}