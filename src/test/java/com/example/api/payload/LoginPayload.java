package com.example.api.payload;

import com.example.api.model.Login;

public class LoginPayload {
    public static Login loginpayload(String username,String password) {
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(password);
        return login;
    }
}