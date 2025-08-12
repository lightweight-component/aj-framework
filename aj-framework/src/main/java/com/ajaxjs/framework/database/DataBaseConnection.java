package com.ajaxjs.framework.database;

import com.ajaxjs.framework.mvc.GlobalExceptionHandler;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.util.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 1、数据库连接、关闭连接；2、数据库事务
 * <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html">...</a>
 * <p>
 * Spring MVC 不能只针对控制器方法进行拦截，而是类似 Servlet Filter 那样的 URL 的拦截。于是静态网页的也会拦截到。
 * 这对性能是减分的，多余的。好像
 * DefaultAnnotationHandlerMapping和AnnotationMethodHandlerAdapter
 * 可以对控制器方法进行拦截，但貌似过时和不知道怎么用 AOP 在控制器上不能直接用，因为控制器的方法都是经过代理包装的
 * <a href="http://www.blogjava.net/atealxt/archive/2009/09/20/spring_mvc_annotation_interceptor_gae.html">...</a>
 * <p>
 * 关于springmvc拦截器不拦截jsp页面的折腾
 * <a href="https://blog.csdn.net/qq_21294095/article/details/85019603">...</a>
 *
 * @author Frank Cheung
 */
@Slf4j
public class DataBaseConnection implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (DispatcherType.ERROR.equals(req.getDispatcherType()) && "/error".equals(req.getRequestURI()))
            // Error page
            return true;

        if (handler instanceof HandlerMethod) {
            boolean noIgnoreDataBaseConnect = DiContextUtil.getAnnotationFromMethod(handler, IgnoreDataBaseConnect.class) == null;
            boolean hasEnableTransaction = DiContextUtil.getAnnotationFromMethod(handler, EnableTransaction.class) != null;

            Connection connection = null;

            // 默认所有控制器方法，要连接数据库，除了带 IgnoreDataBaseConnect 注解的
            if (noIgnoreDataBaseConnect)
                connection = initDb();

            if (connection != null && hasEnableTransaction) {
                try {
                    connection.setAutoCommit(false);
                } catch (SQLException e) {
                    log.error("Error when opening the transaction.", e);
                }
            }
        }

        return true;
    }


    public static Connection initDb() {
        DataSource ds = DiContextUtil.getBean(DataSource.class);
        Objects.requireNonNull(ds, "未配置数据源");
        Connection conn = null;

        try {
            conn = ds.getConnection();
            JdbcConnection.setConnection(conn); // 设置连接到库，使其可用

            if (Version.isDebug)
                log.info("Database [{}...] connected.", conn.getMetaData().getURL().substring(0, 60));
        } catch (SQLException e) {
            log.error("Error when init database connection.", e);
        }

        return conn;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, @Nullable Exception ex) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            if (method.getAnnotation(IgnoreDataBaseConnect.class) == null) {// 有注解
                try {
                    if (method.getAnnotation(EnableTransaction.class) != null) {
                        if (ex == null) { // Global Exception just done.
                            Throwable ex2 = GlobalExceptionHandler.EXCEPTION_HOLDER.get();
                            doTransaction(ex2);
                            GlobalExceptionHandler.EXCEPTION_HOLDER.remove();
                        } else
                            doTransaction(ex);
                    }
                } catch (Throwable e) {
                    log.error("Error when finishing the transaction.", e);
                } finally {
                    JdbcConnection.closeDb();
                }
            }

        }
    }

    private static void doTransaction(Throwable ex) throws SQLException {
        log.info("正在处理数据库事务……");
        Connection conn = JdbcConnection.getConnection();

        if (conn.isClosed())
            throw new SQLException("数据库连接已经关闭");

        if (conn.getAutoCommit())
            throw new SQLException("数据库连接没有关闭自动提交事务");

        if (ex != null)
            conn.rollback();
        else
            conn.commit();

        conn.setAutoCommit(true);
    }

    /**
     * 手动创建连接池。这里使用了 Tomcat JDBC Pool
     *
     * @param driver   驱动程序，如 com.mysql.cj.jdbc.Driver
     * @param url      数据库连接字符串
     * @param userName 用户
     * @param password 密码
     * @return 数据源
     */
    public static DataSource setupJdbcPool(String driver, String url, String userName, String password) {
        PoolProperties p = new PoolProperties();
        p.setDriverClassName(driver);
        p.setUrl(url);
        p.setUsername(userName);
        p.setPassword(password);
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setMaxIdle(30);
        p.setJmxEnabled(false);
        p.setMinIdle(5);
        p.setTestOnBorrow(true);
        p.setTestWhileIdle(true);
        p.setTestOnReturn(true);
        p.setValidationInterval(18800);
        p.setDefaultAutoCommit(true);
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
        ds.setPoolProperties(p);
//        registerMBean(ds);

        return ds;
    }

    /**
     * 手动创建 MySql 连接池。这里使用了 Tomcat JDBC Pool
     *
     * @param url      数据库连接字符串
     * @param userName 用户
     * @param password 密码
     * @return 数据源
     */
    public static DataSource setupMySqlJdbcPool(String url, String userName, String password) {
        return setupJdbcPool("com.mysql.cj.jdbc.Driver", url, userName, password);
    }
}
