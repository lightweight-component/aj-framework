package com.ajaxjs.business.datastru.timer_tick;

import lombok.Data;

@Data
public class Ele {
    private long ctime;
    private long key;

    public Ele(long key) {
        this.key = key;
        this.ctime = System.currentTimeMillis();
    }
}
