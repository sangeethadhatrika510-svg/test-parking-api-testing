package com.example.api.payload;

import com.example.api.model.Register;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.ArrayList;

public class RegisterPayload {

    public static Register registerpayload(String username, String email, String password, ArrayList<String> roles ) throws JsonProcessingException {

       Register reg=new Register();
       reg.setUsername(username);
       reg.setEmail(email);
       reg.setPassword(password);
       reg.setRoles(roles);

       return reg;
    }
}
