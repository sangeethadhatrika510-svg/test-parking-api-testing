package com.example.api.payload;

import com.example.api.model.Register;
import java.util.ArrayList;
import java.util.List;

public class RegisterPayload {

    public static Register registerPayload(String username, String email, String password, List<String> roles) {
        Register register = new Register();
        register.setUsername(username);
        register.setEmail(email);
        register.setPassword(password);
        register.setRoles(new ArrayList<>(roles));
        return register;
    }
}
