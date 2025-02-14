package com.boot.swlugweb.v1.signup;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SignupConfig {
//회원가입
    @Bean
    public PasswordEncoder passwordEncoder() { //비밀번호 암호화
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain signupFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/signup/**")
//                .csrf((csrf) -> csrf.disable()) //csrf 보호 비활성화
//                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()  //해당 경로 모든 요청 허용
//                );
//
//        return http.build();
//    }

}
