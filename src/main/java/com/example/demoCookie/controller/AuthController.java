package com.example.demoCookie.controller;

import com.example.demoCookie.config.jwt.JwtUtils;
import com.example.demoCookie.config.service.RefreshTokenService;
import com.example.demoCookie.config.service.UserDetailsImpl;
import com.example.demoCookie.exception.TokenRefreshException;
import com.example.demoCookie.model.RefreshToken;
import com.example.demoCookie.model.Users;
import com.example.demoCookie.payload.request.SigninRequest;
import com.example.demoCookie.payload.response.JwtTokenResponse;
import com.example.demoCookie.payload.response.MessageResponse;
import com.example.demoCookie.payload.response.TokenRefreshResponse;
import com.example.demoCookie.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request, HttpServletResponse response){
        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
                    request.getPassword()));
            System.out.println("User password: " + request.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String accessToken = jwtUtils.generateJwtToken(userDetails);
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(3600);
            response.addCookie(accessTokenCookie);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            Cookie refreshTokenCookie = new Cookie("refreshTokenCookie", refreshToken.getToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 3600);
            response.addCookie(refreshTokenCookie);

            String roleName = userDetails.getAuthorities().stream().toString();

            return ResponseEntity.ok(new JwtTokenResponse("Login successful!", accessToken,
                userDetails.getUsername(), roleName));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu!");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody Users user){
        if(userRepository.existsByUsername(user.getUsername())){
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if(userRepository.existsByEmail(user.getEmail())){
            return ResponseEntity.badRequest().body("Error: Email is already taken!");
        }

        Users users = new Users(user.getUsername(), encoder.encode(user.getPassword()), user.getEmail(), user.getRoleId());
        userRepository.save(users);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpServletResponse response){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in");
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            refreshTokenService.deleteByUserId(userId);

            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);

            Cookie refreshTokenCookie = new Cookie("refreshTokenCookie", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);

            response.addCookie(refreshTokenCookie);
            response.addCookie(accessTokenCookie);
            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        }catch (Exception e){
            return new ResponseEntity<>("Logout failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshTokenCookie".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is missing in cookies.");
        }

        final String finalRefreshToken = refreshToken;

        return refreshTokenService.findByToken(refreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUserId)
            .flatMap(userId -> userRepository.findById(userId)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, finalRefreshToken));
                }))
            .orElseThrow(() -> new TokenRefreshException(finalRefreshToken, "Refresh token is not in the database!"));
    }

}
