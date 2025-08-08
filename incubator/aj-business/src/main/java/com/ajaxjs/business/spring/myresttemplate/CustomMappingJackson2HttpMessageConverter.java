package com.ajaxjs.business.spring.myresttemplate;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 将Content-Type:"text/html"转换为Map类型格式
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
    public CustomMappingJackson2HttpMessageConverter() {
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.TEXT_HTML);  //加入text/html类型的支持
        setSupportedMediaTypes(mediaTypes);// tag6
    }
}