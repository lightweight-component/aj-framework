package com.ajaxjs.business.json.simple5;

public class Primary implements Json, Value {
    private String value;

    public Primary(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Object value() {
        return value;
    }
}
