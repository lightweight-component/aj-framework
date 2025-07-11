package com.ajaxjs.spring.mvc.unifiedreturn;

import org.springframework.core.convert.converter.Converter;

public abstract class CustomReturnConverter<T> implements Converter<ResponseResultWrapper, T> {
}
