package com.example.demoCookie.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private Long roleId;

}
