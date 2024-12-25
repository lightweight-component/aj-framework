package org.example.config;

import org.example.model.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MyBeanC extends MappingJackson2HttpMessageConverter {
    public MyBeanC() {
        super();
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        HttpHeaders headers = inputMessage.getHeaders();
        if (!headers.isEmpty() &&headers.containsKey("dodecode")) {
            List<String> dodecode = headers.get("dodecode");


            if ("true".equals(dodecode.get(0))) {
                DecodeDTO decodeDTO = getObjectMapper().readValue(inputMessage.getBody(), DecodeDTO.class);
                User user = new User();
                user.setAge(1);
                user.setName("tom");

                return user;
            }
        }

        return super.read(type, contextClass, inputMessage);
    }
}
