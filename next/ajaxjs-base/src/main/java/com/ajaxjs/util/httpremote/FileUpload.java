package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.io.FileHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class FileUpload {
    /**
     * 多段 POST 的分隔，request 头和上传文件内容之间的分隔符
     */
    private static final String DIV_FIELD = "\r\n--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s";

    /**
     * 换行符
     */
    private static final String NEWLINE = "\r\n";

    private static final String BOUNDARY_PREFIX = "--";

    /**
     * 定义数据分隔线
     */
    public static String BOUNDARY = "------------7d4a6d158c9";

    private static final String FIELD = BOUNDARY_PREFIX + BOUNDARY + NEWLINE + "Content-Disposition: form-data;name=\"%s\";filename=\"%s\"" + NEWLINE
            + "Content-Type:%s" + NEWLINE + NEWLINE;

    // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
    private static final byte[] END_DATA = (NEWLINE + BOUNDARY_PREFIX + BOUNDARY + BOUNDARY_PREFIX + NEWLINE).getBytes();

    private static final String TPL =
            "--%s\r\n" +
                    "Content-Disposition: form-data;name=\"%s\";filename=\"%s\"\r\n" +
            "Content-Type:%s\r\n\r\n" +
            "%s\r\n" + // data
            "--%s--\r\n";

    /**
     * 以POST方法上传文件
     *
     * @param url       上传文件的URL
     * @param fieldName 文件字段名
     * @param fileName  文件名
     * @param file      文件内容
     * @param fn        用于设置HTTP连接的回调函数
     * @return 上传成功返回文件上传结果的Map，否则返回null
     */
    public static Map<String, Object> uploadFile(String url, String fieldName, String fileName, byte[] file, Consumer<HttpURLConnection> fn) {
        String field = String.format(FIELD, fieldName, fileName, HttpConstant.FILE_TYPE);// 构造文件字段
        byte[] bytes = concat(field.getBytes(), file);   // 将字段和文件内容拼接
        bytes = concat(bytes, END_DATA);// 拼接结束数据

//        // 如果回调函数不为空，添加设置HTTP请求内容类型的操作
//        if (fn != null)
//            fn = fn.andThen(conn -> conn.setRequestProperty(HttpConstant.CONTENT_TYPE, "multipart/form-data; boundary=" + BOUNDARY));
//            // 如果回调函数为空，直接设置HTTP请求内容类型
//        else
//            fn = conn -> conn.setRequestProperty(HttpConstant.CONTENT_TYPE, "multipart/form-data; boundary=" + BOUNDARY);

        // 发送POST请求并获取响应实体
        Post post = new Post(url, bytes, "multipart/form-data; boundary=" + BOUNDARY, null);

        return post.getResp().responseAsJson();
    }

    /**
     * Map 转换为 byte
     *
     * @param data Map
     * @return Map 转换为 byte
     */
    public static byte[] toFromData(Map<String, Object> data) {
        byte[] bytes = null;

        for (String key : data.keySet()) {
            Object v = data.get(key);
            byte[] _bytes;

            if (v instanceof File) {
                File file = (File) v;
                String field = String.format(FIELD, key, file.getName(), HttpConstant.FILE_TYPE);
                byte[] fileBytes = new FileHelper(file).readFileBytes();

                _bytes = concat(field.getBytes(), fileBytes);
            } else { // 普通字段
                String field = String.format(DIV_FIELD, BOUNDARY, key, v.toString());
                _bytes = field.getBytes();
            }

            if (bytes == null) // 第一次时候为空
                bytes = _bytes;
            else
                bytes = concat(bytes, _bytes);
        }

        assert bytes != null;
        return concat(bytes, END_DATA);
    }

    /**
     * 合并两个字节数组
     *
     * @param a 数组a
     * @param b 数组b
     * @return 新合并的数组
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }
}