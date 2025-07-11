package com.ajaxjs.business.mysqlbinlog.event;

import java.nio.charset.Charset;
import java.util.List;

public interface UpdateEvent {
    String getDatabase();

    String getTable();

    List<String> getFields();

    List<byte[]> getFieldsByte();

    List<String> getOldFields();

    List<byte[]> getOldFieldsByte();

    void setCharset(Charset charset);
}
