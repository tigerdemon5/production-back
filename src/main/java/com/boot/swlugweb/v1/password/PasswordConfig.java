package com.boot.swlugweb.v1.password;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

//비밀번호 수정
@Configuration
@EnableWebSecurity
public class PasswordConfig {

//    @Bean
//    public SecurityFilterChain passwordFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/password/**")
//                .csrf((csrf) -> csrf.disable())
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/password/request-reset").permitAll()
//                        .requestMatchers("/password/reset").permitAll()
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
}