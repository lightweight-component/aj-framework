//package com.ajaxjs.util.http_request;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Slf4j
//public class Get {
//    public static String get(String url) {
//        return get(url, null);
//    }
//
//    public static String get(String url, Consumer<HttpRequest.Builder> init) {
//        HttpRequest.Builder request = HttpRequest.newBuilder().uri(URI.create(url)).GET();
//        if (init != null)
//            init.accept(request);
//
//        //设置建立连接超时 connect timeout
//        var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(3000)).build();
//        HttpResponse<String> response;
//
//        try {
//            response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            String msg = "HTTP GET error. The url is: " + url;
//            log.error(msg, e);
//            throw new RequestException(msg);
//        }
//
//        return response.body();
//    }
//
//    public static String serializeToJson() {
//        return Stream.of("name=zx", "age=19").collect(Collectors.joining(",", "{", "}"));
//    }
//}
