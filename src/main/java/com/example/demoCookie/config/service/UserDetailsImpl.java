package com.example.demoCookie.config.service;

import com.example.demoCookie.model.Roles.Role;
import com.example.demoCookie.model.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String email;

    public Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password,
        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Users user){
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = mapRoleIdToRoleName(user.getRoleId());
        authorities.add(new SimpleGrantedAuthority(roleName));

        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    private static String mapRoleIdToRoleName(Long roleId) {
        if(roleId == 1){
            return String.valueOf(Role.STAFF);
        } else if (roleId == 2) {
            return String.valueOf(Role.MANAGER);
        }else{throw new IllegalArgumentException("Invalid role Id");}
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
