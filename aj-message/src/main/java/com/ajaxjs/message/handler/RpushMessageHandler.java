package com.ajaxjs.message.handler;


import com.ajaxjs.message.model.NormalMessageDTO;
import com.ajaxjs.message.model.RpushMessageDTO;
import com.ajaxjs.message.model.enumration.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Socket消息处理
 *
 * @author 钟宝林
 * @since 2021/2/28/028 21:27
 **/
@Component
public class RpushMessageHandler extends MessageHandler<RpushMessageDTO> {
    @Autowired
    private MessagePushService messagePushService;

    @Override
    public MessageType messageType() {
        return MessageType.RPUSH_SERVER;
    }

    @Override
    public void handle(RpushMessageDTO param) {
        List<String> sendTos = param.getReceiverIds();

        for (String sendTo : sendTos) {
            if (!StringUtils.isNumeric(sendTo))
                continue;

            NormalMessageDTO build = NormalMessageDTO.builder()
                    .fromTo(param.getFromTo())
                    .sendTo(Long.parseLong(sendTo))
                    .build();
            build.setContent(param.getContent());

            messagePushService.push(build);
        }
    }
}
