package com.ajaxjs.framework.fileupload.filedownload;

import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class Download {
    /**
     * StreamingResponseBody是Spring框架从4.2版本增加的一个个用于处理异步响应的接口,特别适用于需要流式传输大文件或大量数据的场景。
     * 它允许开发者直接将数据写入HTTP响应的输出流,而无需将整个响应内容加载到内存中,
     * 尤其是在处理大文件下载或导出时,从而避免了内存溢出,并提高了程序性能
     * <a href="https://mp.weixin.qq.com/s/Q88V8wYRaEduRSZHE0XKFQ">...</a>
     * <a href="https://mp.weixin.qq.com/s/jvPQH7Wzue1eRl2R51ZXIQ">...</a>
     * <a href="https://github.com/Linyuzai/concept/wiki/Concept-Download-2">...</a>
     * <a href="https://mp.weixin.qq.com/s/ZF6V_mhdK3ZaUnQSRoMpTQ">...</a>
     *
     * @return ResponseEntity
     */
    ResponseEntity<?> down() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("orders_" + System.currentTimeMillis() + ".xlsx").build());

        StreamingResponseBody body = outputStream -> {
        };
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

}
