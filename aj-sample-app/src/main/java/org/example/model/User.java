package org.example.model;

import com.ajaxjs.api.encryptedbody.EncryptedData;

// User.java
@EncryptedData
public class User {
    private String name;
    private int age;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}