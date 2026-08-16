package com.ajaxjs.message.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TypeMessageDTO {
    /**
     * 配置id，可以不传，传了会根据对应的配置去发消息
     */
    private List<Long> configIds;

    /**
     * 具体的参数
     */
//    private JSONObject param;
}
