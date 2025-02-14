package com.boot.swlugweb.v1.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class LoginConfig {

    //로그인 수정
//    @Bean
//    public SecurityFilterChain loginFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/login/**", "/password/**")  // Updated paths
//                .csrf((csrf) -> csrf.disable())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                        .maximumSessions(1)
//                        .maxSessionsPreventsLogin(false)
//                )
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/login").permitAll()
//                        .requestMatchers("/login/check").permitAll()
//                        .requestMatchers("/login/logout").permitAll()
//                        .requestMatchers("/password/verify").permitAll()
//                        .requestMatchers("/password/verify-auth").permitAll()
//                        .requestMatchers("/password/reset").permitAll()
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }


    // HTTP 세션 이벤트 리스너 설정
//    @Bean
//    public HttpSessionEventPublisher httpSessionEventPublisher() {
//        return new HttpSessionEventPublisher();
//    }
}