package com.ajaxjs.message.handler;

import com.ajaxjs.message.model.BaseMessage;
import com.ajaxjs.message.model.MessagePushDTO;
import com.ajaxjs.message.model.TypeMessageDTO;
import com.ajaxjs.message.model.enumration.MessageType;
import com.ajaxjs.util.reflect.NewInstance;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 消息处理器基类
 *
 * @author 钟宝林
 * @date 2021/2/8 20:25
 **/
@Slf4j
public abstract class MessageHandler<T extends BaseMessage> implements EventHandler<MessagePushDTO> {
    @Autowired
    protected IRpushPlatformConfigService rpushPlatformConfigService;

    @Autowired
    protected IRpushMessageHisService rpushMessageHisService;

    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(MessagePushDTO event, long sequence, boolean endOfBatch) throws Exception {
        Map<MessageType, TypeMessageDTO> messageParamMap = event.getMessageParam();

        if (!messageParamMap.containsKey(messageType()))
            // 不处理
            return;

        MessageType messageType = messageType();
        TypeMessageDTO typeMessageDTO = messageParamMap.get(messageType);

        if (typeMessageDTO == null)
            return;

        try {
            // 处理参数
            JSONObject param = typeMessageDTO.getParam();
            Class<?> actualTypeArgument = MessageHandlerUtils.getParamType(this);
            BaseMessage baseMessage = param == null ? (BaseMessage) new NewInstance<>(actualTypeArgument).newInstance() : (BaseMessage) param.toBean(actualTypeArgument);
            baseMessage.setRequestNo(event.getRequestNo());
            baseMessage.setClientId(event.getClientId());
            baseMessage.setConfigIds(typeMessageDTO.getConfigIds());

            // 最后调用实际消息处理的方法
            handle((T) baseMessage);
        } catch (Exception e) {
            log.error("消息处理异常", e);
        }
    }

    /**
     * 所有消息处理器必须实现这个接口，标识自己处理的是哪个消息类型
     */
    public abstract MessageType messageType();

    /**
     * 实现这个接口来处理消息，再正式调用这个方法之前会处理好需要的参数和需要的配置
     */
    public abstract void handle(T param);

}
