package com.example.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Register {
 String username;
 String email;
 String password;
 ArrayList<String> roles;
}

