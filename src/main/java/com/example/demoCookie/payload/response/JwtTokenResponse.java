package com.example.demoCookie.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenResponse {
    private String message;
    private String token;
    private String type = "Bearer";
    private String username;
    private String roleName;

    public JwtTokenResponse(String message,String token, String username, String roleName) {
        this.message = message;
        this.token = token;
        this.username = username;
        this.roleName = roleName;
    }
}
