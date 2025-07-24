package com.ajaxjs.business.datastru.smtp;

import lombok.Data;

@Data
public class Request {
    String sender;
    String[] receiver;
    String subject;
    String[] data;
}
