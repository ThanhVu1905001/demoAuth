package com.example.demoCookie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private Long roleId;

    public Users(String username, String password, String email, Long roleId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roleId = roleId;
    }
}
