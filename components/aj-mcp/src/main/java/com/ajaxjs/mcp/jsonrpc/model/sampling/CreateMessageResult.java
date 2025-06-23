package com.ajaxjs.mcp.jsonrpc.model.sampling;


import com.ajaxjs.mcp.jsonrpc.model.Role;
import com.ajaxjs.mcp.jsonrpc.model.content.Content;
import lombok.Data;

@Data
public class CreateMessageResult {
     Role role;

     Content content;

     String model;

     StopReason stopReason;
}
