package com.ajaxjs.message.model.config;

import com.ajaxjs.message.model.enumration.MessagePlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 更新配置DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConfigDTO {
    private MessagePlatformEnum platform;

    private Long configId;

    private String configName;

    /**
     * 键对应配置的 key，值对应配置的 value
     */
    private Map<String, String> config;
}
