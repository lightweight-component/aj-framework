package org.example.model;

//import com.ajaxjs.api.encryptedbody.EncryptedData;
//import com.ajaxjs.desensitize.DesensitizeType;
//import com.ajaxjs.desensitize.annotation.DesensitizeModel;
//import com.ajaxjs.desensitize.annotation.DesensitizeProperty;
import lombok.Data;

//@EncryptedData
@Data
//@DesensitizeModel
public class User {
    private String name;

//    @DesensitizeProperty(DesensitizeType.PHONE)
    private String phone;

    private int age;
}