package com.ajaxjs.message.email;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RegExpUtils;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

/**
 * 简易邮件发送器
 * 基于 SMTP 协议，支持 SSL/TLS
 */
@Slf4j
public class SenderSSL {
    private SSLSocket socket;


    public SenderSSL(MailWithConfig bean) throws IOException {
        this.bean = bean;
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.socket = (SSLSocket) factory.createSocket(bean.getMailServer(), bean.getPort());

        // 初始化 IO 流
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.os = new DataOutputStream(socket.getOutputStream());
    }

    public static final String LINEFEET = "\r\n"; // 换行符常量

    private static final int OK_250_CODE = 250;// 成功标识

    private final MailWithConfig bean; // 邮件信息

    private BufferedReader in; // 接受指令用的缓冲区

    private DataOutputStream os; // 发送指令用的流

    /**
     * 发送邮件
     *
     * @return 是否成功
     * @throws MailException 邮件异常
     */
    public boolean sendMail() throws MailException {
        log.info("发送邮件:" + bean.getSubject());

        try {
            // 对于 SSLSocket，需要手动开始握手
            socket.startHandshake();
            log.info("SSL/TLS Handshake completed.");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
                this.in = in;
                this.os = os;

                String result = in.readLine();// 初始化连接
                if (!isOkCode(result, 220))
                    throw new MailException("初始化连接：" + result, 220);

                // 进行握手
                // 对于 SSL 连接，通常使用 EHLO 替代 HELO，以便服务器返回支持的扩展，包括 AUTH LOGIN
                result = sendCommand("EHLO %s", bean.getMailServer());

                if (!isOkCode(result, OK_250_CODE))
                    throw new MailException("EHLO 握手失败：" + result, OK_250_CODE);

                // 判断是否支持 STARTTLS
                if (result.contains("STARTTLS")) {
                    sendCommand("STARTTLS");
                    String tlsResult = readMultiLineResponse();

                    if (!isOkCode(tlsResult, 220))
                        throw new MailException("STARTTLS 启动失败：" + tlsResult, 220);

                    socket.startHandshake();

                    // 重新包装 IO 流
                    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    this.os = new DataOutputStream(socket.getOutputStream());
                }

                // 验证发信人信息
                result = sendCommand("AUTH LOGIN");
                if (!isOkCode(result, 334))
                    throw new MailException("验证发信人信息失败：" + result, 334);

                result = sendCommand(toBase64(bean.getAccount()));
                if (!isOkCode(result, 334))
                    throw new MailException("发信人名称发送失败：" + result, 334);

                if (ObjectHelper.isEmptyText(bean.getPassword()))
                    log.warn("No password set for SMTP.");

                result = sendCommand(toBase64(bean.getPassword()));
                if (!isOkCode(result, 235))
                    throw new MailException("認証不成功" + result, 235);

                // 发送指令
                result = sendCommand("Mail From:<%s>", bean.getFrom());
                if (!isOkCode(result, OK_250_CODE))
                    throw new MailException("发送指令 From 不成功" + result, OK_250_CODE);

                result = sendCommand("RCPT TO:<%s>", bean.getTo());
                if (!isOkCode(result, OK_250_CODE))
                    throw new MailException("发送指令 To 不成功" + result, OK_250_CODE);

                result = sendCommand("DATA");
                if (!isOkCode(result, 354))
                    throw new MailException("DATA 命令失败：" + result, 354);

                String data = data();
                log.info("Email Data:\n" + data); // 打印邮件数据，方便调试

                result = sendCommand(data);
                if (!isOkCode(result, OK_250_CODE))
                    throw new MailException("发送邮件失败：" + result, OK_250_CODE);

                result = sendCommand("QUIT");// quit
                if (!isOkCode(result, 221))
                    throw new MailException("QUIT 失败：" + result, 221);

            }
        } catch (UnknownHostException e) {
            System.err.println("初始化失败！建立连接失败！");
            log.warn("Error:", e);
            return false;
        } catch (IOException e) {
            System.err.println("初始化失败！读取流失败或SSL握手失败！");
            log.warn("Error:", e);
            return false;
        } catch (MailException e) {
            log.warn("Mail sending failed: " + e.getMessage(), e);
            return false;
        } finally {
            try {
                if (!socket.isClosed())  // 避免重复关闭
                    socket.close();
            } catch (IOException e) {
                log.warn("Error closing socket:", e);
            }
        }

        return true;
    }

    private String readMultiLineResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);

            // 多行响应结束标志：以数字开头且后面没有 '-'
            if (line.length() >= 4 && Character.isDigit(line.charAt(0)) && line.charAt(3) == ' ') {
                break;
            }

            sb.append(LINEFEET); // 拼接换行符
        }

        return sb.toString();
    }

    /**
     * 生成正文
     *
     * @return 正文
     */
    private String data() {
        String boundary = "------=_NextPart_" + System.currentTimeMillis();
        Map<String, byte[]> attachment = bean.getAttachment();

        StringBuilder sb = new StringBuilder();
        sb.append("From:<").append(bean.getFrom()).append(">").append(LINEFEET);
        sb.append("To:<").append(bean.getTo()).append(">").append(LINEFEET);
        sb.append("Subject:=?UTF-8?B?").append(toBase64(bean.getSubject())).append("?=").append(LINEFEET);
        // sb.append("Date:2016/10/27 17:30" + LINEFEET);
        // sb.append("MIME-Version: 1.0" + lineFeet); // MIME-Version 是必需的头

        if (attachment != null && !attachment.isEmpty()) { // 确保有附件才添加 multipart
            sb.append("MIME-Version: 1.0").append(LINEFEET); // 多部分邮件需要 MIME-Version
            sb.append("Content-Type: multipart/mixed;boundary=\"").append(boundary).append("\"").append(LINEFEET);
            sb.append(LINEFEET);
            sb.append("--").append(boundary).append(LINEFEET);
        } else {
            // 如果没有附件，Content-Type 直接放在这里
            sb.append(bean.isHtmlBody() ? "Content-Type:text/html;charset=\"utf-8\"" : "Content-Type:text/plain;charset=\"utf-8\"").append(LINEFEET);
        }

        // 正文部分
        if (attachment != null && !attachment.isEmpty()) { // 如果是多部分邮件，需要再次指定正文的 Content-Type
            sb.append(bean.isHtmlBody() ? "Content-Type:text/html;charset=\"utf-8\"" : "Content-Type:text/plain;charset=\"utf-8\"").append(LINEFEET);
            sb.append("Content-Transfer-Encoding: base64" + LINEFEET);
            sb.append(LINEFEET);
            sb.append(toBase64(bean.getContent()));
        } else { // 如果是单部分邮件，Content-Type 已经加过了
            sb.append("Content-Transfer-Encoding: base64" + LINEFEET);
            sb.append(LINEFEET);
            sb.append(toBase64(bean.getContent()));
        }


        // 附件部分
        if (attachment != null && !attachment.isEmpty()) {
            for (String fileName : attachment.keySet()) {
                sb.append(LINEFEET); // 确保与前一个部分的结束之间有空行
                sb.append("--").append(boundary).append(LINEFEET);
                // 修正 Content-Type，根据文件名推断更好，这里暂时用 octet-stream
                sb.append("Content-Type: application/octet-stream; name=\"").append(fileName).append("\"").append(LINEFEET);
                sb.append("Content-Disposition: attachment; filename=\"").append(fileName).append("\"").append(LINEFEET);
                sb.append("Content-Transfer-Encoding: base64").append(LINEFEET);
                sb.append(LINEFEET);
                sb.append(Base64.getEncoder().encodeToString(attachment.get(fileName)));
            }
            sb.append(LINEFEET).append("--").append(boundary).append("--"); // 多部分邮件的结束边界
        }

        sb.append(LINEFEET + "."); // SMTP DATA 结束符

        return sb.toString();
    }

    public static String readFile(String filePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送smtp指令 并返回服务器响应信息
     *
     * @param string 指令
     * @param from   指令参数
     * @return 服务器响应信息
     */
    private String sendCommand(String string, String from) {
        return sendCommand(String.format(string, from));
    }

    /**
     * 发送smtp指令 并返回服务器响应信息
     *
     * @param msg 指令，会在字符串后面自动加上 lineFeet
     * @return 服务器响应信息
     */
    private String sendCommand(String msg) {
        try {
            os.writeBytes(msg + LINEFEET);
            os.flush();

            return in.readLine(); // 读取服务器端响应信息
        } catch (IOException e) {
            log.warn("Error sending command or reading response:", e);
            return null;
        }
    }

    /**
     * Base64 编码的一种实现
     *
     * @param str 待编码的字符串
     * @return 已编码的字符串
     */
    public static String toBase64(String str) {
        // 假设 EncodeTools.base64EncodeToString 已经处理了 null 和编码问题
        if (str == null)
            return ""; // 或者抛出异常，取决于 EncodeTools 的行为

        return EncodeTools.base64EncodeToString(str);
    }

    /**
     * 输入期望 code，然后查找字符串中的数字，看是否与之匹配。匹配则返回 true。
     *
     * @param str  输入的字符串，应该要包含数字
     * @param code 期望值
     * @return 是否与之匹配
     */
    private static boolean isOkCode(String str, int code) {
        if (str == null)
            return false; // 如果字符串是 null，直接返回 false

        String matchedCodeStr = RegExpUtils.regMatch("^\\d+", str);

        if (matchedCodeStr == null || matchedCodeStr.isEmpty())
            return false; // 没有匹配到数字，返回 false

        int _code = Integer.parseInt(matchedCodeStr);

        return _code == code;
    }

    /**
     * 发送邮件
     *
     * @param mail 服务器信息和邮件信息
     * @return true 表示为发送成功，否则为失败
     */
    public static boolean send(MailWithConfig mail) {
        try {
            SenderSSL sender = new SenderSSL(mail);
            return sender.sendMail();
        } catch (IOException | MailException e) {
            log.warn("Error when sending email", e);
            return false;
        }
    }
}
