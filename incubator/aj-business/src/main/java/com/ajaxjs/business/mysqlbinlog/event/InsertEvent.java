package com.ajaxjs.business.mysqlbinlog.event;

import java.nio.charset.Charset;
import java.util.List;

public interface InsertEvent {
    String getDatabase();

    String getTable();

    List<String> getFields();

    List<byte[]> getFieldsByte();

    void setCharset(Charset charset);
}
