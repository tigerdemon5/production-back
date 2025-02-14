package com.boot.swlugweb.v1.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Properties;

//이메일 수정
// 사용자 이메일로 인증 번호 담긴 메일을 보내기 위한 설정 진행
@Configuration
public class EmailConfig {

    @Value("${SMTP_PW}")
    private String smtpPw;

    @Bean //Java Mail Sender인터페이스를 구현한 객체를 Bean으로 등록
    public JavaMailSender mailSender(){

        //JavaMailSender의 구현체를 생성 후 속성 지정
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.naver.com"); //이메일 전송에 사용할 SMTP 서버 호스트 설정
        mailSender.setPort(465); //포트 지정
        mailSender.setUsername("likeeu23@naver.com");//구글 계정 받기
        mailSender.setPassword(smtpPw); //구글 앱 비번 넣기

        //JavaMail 속성 설정을 위해 Properties 객체 생성
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.transport.protocol", "smtp");
        javaMailProperties.put("mail.smtp.auth", true); //smtp 서버 인증에 필요
        javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL 소켓 팩토리 클래스 사용
        javaMailProperties.put("mail.smtp.starttls.enable", true); //starttls(tls 시작 명령) 사용, 암호화된 통신 시작
        javaMailProperties.put("mail.debug", true);
        javaMailProperties.put("mail.smtp.ssl.trust", "smtp.naver.com"); //smtp 서버의 ssl 인증서를 신뢰
        javaMailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2"); //사용할 ssl 프로토콜 버전

        //mailsender에 우리가 만든 properties 넣기
        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender; //mailSender를 빈으로 등록
    }

//    @Bean
//    public SecurityFilterChain emailFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/email/**")
//                .csrf((csrf) -> csrf.disable()) //csrf 보호 비활성화
//                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()  //해당 경로 모든 요청 허용
//                );
//
//        return http.build();
//    }
}
