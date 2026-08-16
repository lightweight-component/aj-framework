/**
 * 版权所有 2017 Sp42 frank@ajaxjs.com 根据 2.0 版本 Apache 许可证("许可证")授权；
 * 根据本许可证，用户可以不使用此文件。 用户可从下列网址获得许可证副本：
 * http://www.apache.org/licenses/LICENSE-2.0
 * 除非因适用法律需要或书面同意，根据许可证分发的软件是基于"按原样"基础提供，
 * 无任何明示的或暗示的保证或条件。详见根据许可证许可下，特定语言的管辖权限和限制。
 */
package com.ajaxjs.framework.fileupload.s3service;

import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.httpremote.Delete;
import com.ajaxjs.util.httpremote.Get;
import com.ajaxjs.util.httpremote.Put;
import com.ajaxjs.util.httpremote.Response;
import com.ajaxjs.util.io.FileHelper;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * 已停服的网易云对象存储（NOS）HTTP 适配器。
 * @deprecated 服务商已停止该对象存储服务，仅为旧系统兼容保留。
 */
@Deprecated
public class NsoHttpUpload implements IFileUpload {
    /**
     * App ID
     */
    @Value("${S3Storage.Nso.accessKey}")
    private String accessKeyId;

    /**
     * App 密钥
     */
    @Value("${S3Storage.Nso.accessSecret}")
    private String accessSecret;

    @Value("${S3Storage.Nso.api}")
    private String api;

    @Value("${S3Storage.Nso.bucket}")
    private String bucket;

    /**
     * 列出账号下的存储桶。
     *
     * @return XML 响应转换后的键值映射
     */
    public Map<String, String> listBuk() {
        String now = DateTools.nowGMTDate();
        String canonicalHeaders = "", canonicalResource = "/";
        String data = "GET\n" + "\n" + "\n" + now + "\n" + canonicalHeaders + canonicalResource;

        return Get.apiXml("http://nos-eastchina1.126.net", conn -> {
            conn.addRequestProperty("Authorization", getAuthorization(data));
            conn.addRequestProperty("Date", now);
            conn.addRequestProperty("Host", "nos-eastchina1.126.net");
        });
    }

    /**
     * 生成验证的字符串
     *
     * @param data 数据
     * @return 验证的字符串
     */
    private String getAuthorization(String data) {
        String signature = HashHelper.getHmacSHA256(data, accessSecret, false);

        return "NOS " + accessKeyId + ":" + signature;
    }

    /**
     * 创建零字节对象。
     *
     * @param filename 对象键
     */
    public void createEmptyFile(String filename) {
        String now = DateTools.nowGMTDate();
        String canonicalHeaders = "", canonicalResource = "/" + bucket + "/" + filename;
        String data = "PUT\n" + "\n\n" + now + "\n" + canonicalHeaders + canonicalResource;

        Put.api(api + filename, new byte[0], conn -> {
            conn.addRequestProperty("Authorization", getAuthorization(data));
            conn.addRequestProperty("Content-Length", "0");
            conn.addRequestProperty("Date", now);
//			conn.addRequestProperty("Host", "ajaxjs.nos-eastchina1.126.net");
        });
    }

    /**
     * 删除指定对象。
     *
     * @param filename 要删除的文件名称。
     * @return 当前实现固定返回 {@code false}，不能据此判断服务端删除结果
     */
    public boolean delete(String filename) {
        String now = DateTools.nowGMTDate();// 获取当前时间，用于请求头
        String canonicalHeaders = "", canonicalResource = "/" + bucket + "/" + filename;// 构建规范化的请求头和资源路径
        String data = "DELETE\n" + "\n\n" + now + "\n" + canonicalHeaders + canonicalResource; // 构建用于授权认证的数据字符串

        Delete.api(api + filename, conn -> { // 发起 DELETE 请求删除文件
            conn.addRequestProperty("Authorization", getAuthorization(data));   // 设置请求授权头和日期头
            conn.addRequestProperty("Date", now);
        });

        return false;  // 当前实现总是返回 false
    }

    /**
     * 使用本地文件名上传文件。
     *
     * @param filePath 本地文件路径
     * @return 上传成功时返回 {@code true}
     */
    public boolean uploadFile(String filePath) {
        return uploadFile(null, filePath);
    }

    /**
     * 上传本地文件，并可指定对象键。
     *
     * @param filePath 本地文件路径
     * @param filename 对象键；为 {@code null} 时使用本地文件名
     * @return 服务确认上传成功时返回 {@code true}
     */
    public boolean uploadFile(String filePath, String filename) {
        File file = new File(filePath); // 创建一个File对象，用于表示文件路径

        if (filename == null)   // 如果未指定文件名，则使用原文件名
            filename = file.getName();

        // 将文件以字节流形式打开，并计算其 MD5 值，然后调用 upload 方法进行上传
        try {
            return upload(new FileHelper(file).readFileBytes(), filename, HashHelper.calcFileMD5(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用预先计算的 MD5 上传字节内容。
     *
     * @param bytes 文件内容
     * @param filename 对象键，可包含目录前缀
     * @param md5 内容 MD5
     * @return HTTP 状态和 ETag 均匹配时返回 {@code true}
     */
    public boolean upload(byte[] bytes, String filename, String md5) {
        String now = DateTools.nowGMTDate();
        String canonicalHeaders = "", canonicalResource = "/" + bucket + "/" + filename;
        String data = "PUT\n" + md5 + "\n\n" + now + "\n" + canonicalHeaders + canonicalResource;

        Response result = Put.api(api + filename, bytes, conn -> {
            conn.addRequestProperty("Authorization", getAuthorization(data));
            conn.addRequestProperty("Content-Length", String.valueOf(bytes.length));
            conn.addRequestProperty("Content-MD5", md5);
            conn.addRequestProperty("Date", now);
//			conn.addRequestProperty("HOST", "gdhdc-org.nos-eastchina1.126.net/cover");
            // conn.addRequestProperty("x-nos-entity-type", "json");
        });

        // 判定是否上传成功
        String ETag = result.getConnection().getHeaderField("ETag");

        if (ETag == null)
            return false;

        return result.getHttpCode() == 200 && ETag.equalsIgnoreCase("\"" + md5 + "\"");
    }

    /**
     * 上传字节内容并自动计算 MD5。
     *
     * @param bytes    文件字节数组
     * @param filename 文件名，可在前面设置目录名，如 folder + "/" + saveFileName
     * @return 是否成功
     */
    @Override
    public boolean upload(String filename, byte[] bytes) {
        return upload(bytes, filename, HashHelper.calcFileMD5(bytes));
    }

    /**
     * 截取字节数组中的一段并上传。
     *
     * @param bytes 原始字节数组
     * @param offset 起始偏移
     * @param length 截取长度
     * @param filename 对象键
     * @return 上传成功时返回 {@code true}
     */
    public boolean save(byte[] bytes, int offset, int length, String filename) {
        bytes = subBytes(bytes, offset, length); // 内存中的字节数组上传到空间中

        return upload(filename, bytes);
    }

    /**
     * 复制字节数组的指定区间。
     *
     * @param data 输入数组
     * @param off 起始偏移
     * @param length 复制长度
     * @return 新字节数组
     * @throws IndexOutOfBoundsException 区间越界时抛出
     */
    public static byte[] subBytes(byte[] data, int off, int length) {
        byte[] bs = new byte[length];
        System.arraycopy(data, off, bs, 0, length);

        return bs;
    }
}
