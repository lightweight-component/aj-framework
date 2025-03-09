package com.ajaxjs.rag.controler;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import com.ajaxjs.rag.parser.FileParserFactory;
import com.ajaxjs.rag.service.LLM.OpenAIChatService;
import com.ajaxjs.rag.constant.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatController extends NanoHTTPD {
    // 存储聊天消息的列表
    private static final Map<String, String> chatHistory = new HashMap<>();
    private final OpenAIChatService openAIChatService;

    public ChatController(int port) throws IOException {
        super(port);

        String apiKey = Config.API_KEY; // 初始化 OpenAIChatService
        this.openAIChatService = new OpenAIChatService(apiKey);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started on port " + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        try {
            // 添加 CORS 头信息，允许所有来源的请求
            Response response;
            if (Method.OPTIONS.equals(method)) {
                response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "Content-Type");
                return response;
            }

            if (Method.GET.equals(method) && "/chat".equals(uri)) {
                // 处理 GET 请求，返回聊天历史记录
                StringBuilder html = new StringBuilder();
                html.append("<html><body>");
                for (Map.Entry<String, String> entry : chatHistory.entrySet())
                    html.append("<p>").append(entry.getKey()).append(": ").append(entry.getValue()).append("</p>");

                html.append("</body></html>");
                response = newFixedLengthResponse(html.toString());
            } else if (Method.POST.equals(method) && "/send".equals(uri)) {
                // 处理普通 POST 请求
                session.parseBody(new HashMap<>());
                Map<String, String> params = session.getParms();
                String message = params.get("message");

                if (message != null && !message.isEmpty()) {
                    chatHistory.put("User", message);

                    // 调用 OpenAIChatService 生成回复消息
                    String model = Config.LLM_MODEL;
                    String url = Config.LLM_URL;
                    JSONObject newMessage = new JSONObject().put("role", "user").put("content", message);
                    JSONObject[] messages = new JSONObject[]{newMessage};
                    JSONObject paramsForAPI = new JSONObject()
                            .put("model", model)
                            .put("messages", messages)
                            .put("temperature", 0.3)
                            .put("stream", false);

                    String generatedText = openAIChatService.generateText(url, paramsForAPI);
                    System.out.println(generatedText);
                    chatHistory.put("Bot", generatedText);

                    // 创建一个 JSON 对象，将生成的文本放入其中
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("generatedText", generatedText);
                    // 将 JSON 对象转换为字符串并返回
                    response = newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                } else {
                    response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Message is empty");
                }
            } else if (Method.POST.equals(method) && "/sendFile".equals(uri)) {
                // 检查是否是文件上传请求
                if (session.getHeaders().get("content-type") != null && session.getHeaders().get("content-type").startsWith("multipart/form-data")) {
                    try {
                        Map<String, String> files = new HashMap<>();
                        session.parseBody(files);
                        String isFile = session.getParms().get("isFile");
                        if ("true".equals(isFile)) {
                            String fileParamName = "file";
                            String filePath = files.get(fileParamName);
                            String originalFileName = session.getParms().get("originalFileName");
                            if (filePath != null && originalFileName != null) {
                                try {
                                    // 调用 FileParserFactory 解析文件内容
                                    String content = FileParserFactory.easyParse(filePath, originalFileName);
                                    String processedMessage = "解析文件内容：" + content;
                                    chatHistory.put("User", processedMessage);

                                    // 调用 OpenAIChatService 生成回复消息
                                    String model = Config.LLM_MODEL;
                                    String url = Config.LLM_URL;
                                    JSONObject newMessage = new JSONObject().put("role", "user").put("content", processedMessage);
                                    JSONObject[] messages = new JSONObject[]{newMessage};
                                    JSONObject paramsForAPI = new JSONObject()
                                            .put("model", model)
                                            .put("messages", messages)
                                            .put("temperature", 0.3)
                                            .put("stream", false);

                                    String generatedText = openAIChatService.generateText(url, paramsForAPI);
                                    System.out.println(generatedText);
                                    chatHistory.put("Bot", generatedText);

                                    // 创建一个 JSON 对象，将生成的文本放入其中
                                    JSONObject responseJson = new JSONObject();
                                    responseJson.put("generatedText", generatedText);

                                    // 将 JSON 对象转换为字符串并返回
                                    response = newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                                } catch (Exception e) {
                                    response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "文件解析失败：" + e.getMessage());
                                }
                            } else {
                                response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "未找到上传的文件或原始文件名");
                            }
                        } else {
                            response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "不是文件上传请求");
                        }
                    } catch (Exception e) {
                        response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "处理请求时出错：" + e.getMessage());
                    }
                } else {
                    response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "不是有效的文件上传请求");
                }
            } else {
                response = newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
            }

            // 添加 CORS 头信息
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        } catch (IOException | ResponseException e) {
            // 记录异常信息
            System.err.println("Error while serving request: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error processing request");
        }
    }

    public static void main(String[] args) {
        try {
            new ChatController(8080);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}