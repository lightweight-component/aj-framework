package com.ajaxjs.framework.dataservice;

import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.crud.page.PageResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.util.httpremote.HttpConstant;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.DelegatingServletInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TestDataService {
    static class Dispatcher extends DataServiceDispatcher {

    }

    private HttpServletRequest mockRequest;

    Dispatcher dispatcher;

    @BeforeEach
    void setUp() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
        dispatcher = new Dispatcher();
        dispatcher.endPointMgr = TestCase.initEndpointMgr();
    }

    protected static Connection conn;

    @BeforeAll
    static void setUpAll() throws SQLException {
        // 配置 H2 数据源
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("password");

        // 获取数据库连接
        conn = dataSource.getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE shop_address (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    address VARCHAR(255) NOT NULL,\n" +
                    "    phone VARCHAR(20),\n" +
                    "    receiver VARCHAR(255),\n" +
                    "    stat INT,\n" +
                    "    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                    ");");

            stmt.execute("INSERT INTO shop_address (name, address, phone, receiver, stat)\n" +
                    "VALUES\n" +
                    "('Shop A', '123 Main St', '123-456-7890', 'John Doe', 0),\n" +
                    "('Shop B', '456 Elm St', '234-567-8901', 'Jane Smith',0),\n" +
                    "('Shop C', '789 Oak St', '345-678-9012', 'Alice Johnson', 0),\n" +
                    "('Shop D', '101 Maple St', '456-789-0123', 'Bob Brown', 1),\n" +
                    "('Shop E', '202 Birch St', '567-890-1234', 'Charlie Davis', 1);");
        }

        JdbcConnection.setConnection(conn);
        System.out.println("init ok");

        // 在这里放置一次性初始化代码
//        conn = JdbcConnection.getMySqlConnection(config.get("database.ipPort").toString(), "aj_base",
//                config.get("database.username").toString(), config.get("database.password").toString());
    }

    @AfterAll
    static void end() {
        JdbcConnection.closeDb(conn);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInfoByQueryString() {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"1"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        Object result = dispatcher.request(mockRequest);

        assertNotNull(result);
        assertEquals(1, ((Map<String, Object>) result).get("ID"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInfoByPatchVar() {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/patch/2");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Object result = dispatcher.request(mockRequest);

        assertEquals(2, ((Map<String, Object>) result).get("ID"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testList() {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Object result = dispatcher.request(mockRequest);

        assertEquals(5, ((List<Map<String, Object>>) result).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPage() {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/page");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getParameter("start")).thenReturn("0");
        when(mockRequest.getParameter("limit")).thenReturn("3");

        Object result = dispatcher.request(mockRequest);

        PageResult<Map<String, Object>> result1 = (PageResult<Map<String, Object>>) result;
        assertEquals(3, result1.getList().size());
        assertEquals(5, result1.getTotalCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateForm() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/patch");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_FORM);

        String formDataString = "name=你好&address=secret123";
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(6, (int) result);

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"6"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateJson() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/patch");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_JSON);

        String formDataString = "{\"name\":\"你好\",\"address\":\"secret123\"}";
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(7, (int) result);

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"6"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateJsonBySQL() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/create");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_JSON);

        String formDataString = "{\"name\":\"你好\",\"address\":\"secret123\"}";
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(8, (int) result);

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"6"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateForm() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/patch");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("PUT");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_FORM);

        String formDataString = "name=你好&address=secret123&id=2";
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(1, ((UpdateResult) result).getEffectedRows());

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"2"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateJson() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/patch");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("PUT");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_JSON);

        String formDataString = "{\"name\":\"你好\",\"address\":\"secret123\",\"id\":2}";
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(1, ((UpdateResult) result).getEffectedRows());

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"2"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateJsonPath() throws IOException {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/update/2");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("PUT");
        when(mockRequest.getContentType()).thenReturn(HttpConstant.CONTENT_TYPE_JSON);

        String formDataString = "{\"name\":\"你好\",\"address\":\"secret123\",\"id\":1}"; // ignore this id
        byte[] formDataBytes = formDataString.getBytes(StandardCharsets.UTF_8);
        ServletInputStream servletInputStream = new DelegatingServletInputStream(new ByteArrayInputStream(formDataBytes));
        when(mockRequest.getInputStream()).thenReturn(servletInputStream);

        Object result = dispatcher.request(mockRequest);

        assertEquals(1, ((UpdateResult) result).getEffectedRows());

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"2"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("你好", ((Map<String, Object>) result).get("NAME"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLogicalDelete() {
        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/del");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("DELETE");

        Object result = dispatcher.request(mockRequest);

        assertEquals(1, ((UpdateResult) result).getEffectedRows());

        when(mockRequest.getRequestURI()).thenReturn("/app/ds_api/foo/bar");
        when(mockRequest.getContextPath()).thenReturn("/app");
        when(mockRequest.getMethod()).thenReturn("GET");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{"2"});

        when(mockRequest.getParameterMap()).thenReturn(parameterMap);

        result = dispatcher.request(mockRequest);

        assertEquals("hi", ((Map<String, Object>) result).get("NAME"));
    }
}
