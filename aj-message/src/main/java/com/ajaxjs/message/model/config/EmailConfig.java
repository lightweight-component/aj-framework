package com.ajaxjs.message.model.config;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfig extends Config {
    private String host;
    private int port;
    private String from;
    private String user;
    private String password;
}
