package com.sparta.plusweek.domain.config;

import com.sparta.plusweek.global.error.ExceptionHandleFilter;
import com.sparta.plusweek.domain.jwt.JwtFilter;
import com.sparta.plusweek.domain.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

//JWT 이용하여 인증 및 인가
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 알고리즘으로 암호화 (비밀번호 길이 60 고정)
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public ExceptionHandleFilter exceptionHandleFilter() {
        return new ExceptionHandleFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable); // CSRF 는 쿠키 기반 공격이므로 불필요

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 정적 자원은 허용
                .requestMatchers("/api/auth/**").permitAll() // 로그인 및 회원가입은 허용
                .anyRequest().authenticated()); // 나머지 엔드포인트는 검증

        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(exceptionHandleFilter(), JwtFilter.class);

        return http.build();
    }
}
