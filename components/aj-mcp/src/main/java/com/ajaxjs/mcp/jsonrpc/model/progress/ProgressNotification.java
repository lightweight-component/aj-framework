package com.ajaxjs.mcp.jsonrpc.model.progress;

import lombok.Data;

@Data
public class ProgressNotification {
    String progressToken;

    double progress;

    Double total;
}
