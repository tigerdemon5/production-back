package com.boot.swlugweb.v1.security;

import com.boot.swlugweb.v1.login.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    // 로그인 시도 횟수 제한 등을 처리하기 위한 LoginService 주입
    @Autowired
    private LoginService loginService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        // 커스텀 인증 필터 설정
        SecurityUserPasswordAuthenticationFilter customAuthFilter =
                new SecurityUserPasswordAuthenticationFilter(authenticationManager, loginService);
        customAuthFilter.setFilterProcessesUrl("/api/login");

        http
                // CSRF 보호 기능 비활성화
                .csrf(csrf -> csrf.disable())
                // CORS 설정 활성화
                .cors(Customizer.withDefaults())
                // 기본 로그인 폼 비활성화
                .formLogin(form -> form.disable())
                // 세션 관리 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 필요할 때만 세션 생성
                        .maximumSessions(1)                                        // 동시 세션 제한
                        .maxSessionsPreventsLogin(false)                          // 새로운 로그인 시 이전 세션 만료
                        .sessionRegistry(sessionRegistry())                        // 세션 레지스트리 설정
                        .expiredUrl("/api/login")                                 // 세션 만료시 리다이렉트 URL
                )
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 블로그 관련 권한
                        .requestMatchers("/api/blog/save", "/api/blog/update", "/api/blog/delete", "/api/blog/upload-image").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/blog/**").permitAll()
                        .requestMatchers("/api/blog/detail", "/api/blog/tags", "/api/blog/adjacent").permitAll()

                        // 공지사항 관련 권한
                        .requestMatchers("/api/notice/save", "/api/notice/update", "/api/notice/delete","/api/notice/upload-image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notice/**").permitAll()
                        .requestMatchers("/api/notice/detail", "/api/notice/adjacent").permitAll()

                        // 로그인/회원가입 관련 권한
                        .requestMatchers("/api/login/**").permitAll()
                        .requestMatchers("/api/login/check").permitAll()
                        .requestMatchers("/api/login/logout").permitAll()
                        .requestMatchers("/api/signup/**").permitAll()

                        // 마이페이지 접근 권한
                        .requestMatchers("/api/mypage").authenticated()

                        // 비밀번호 관련 권한
                        .requestMatchers("/api/password/request-reset").permitAll()
                        .requestMatchers("/api/password/reset").permitAll()
                        .requestMatchers("/api/password/verify").permitAll()
                        .requestMatchers("/api/password/verify-auth").permitAll()

                        // 이메일 인증 관련 권한
                        .requestMatchers("/api/email/**").permitAll()

                        // 기타 페이지 접근 권한
                        .requestMatchers("/api/faq","/api/main", "/api/intro", "/api/apply", "/api/privacy" ).permitAll()

                        // 에러 페이지 접근 권한
                        .requestMatchers("/error").permitAll()
                )
                // 커스텀 인증 필터 추가
                .addFilterAt(customAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // CORS 설정을 위한 빈 설정
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")                         // 모든 경로에 대해
                        .allowedHeaders("*")                       // 모든 헤더 허용
                        .allowedOrigins("http://localhost:3000")   // 프론트엔드 주소 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                        .allowCredentials(true)                    // 인증 정보 포함 허용
                        .exposedHeaders("Authorization");          // Authorization 헤더 노출
            }
        };
    }

    // HTTP 세션 이벤트 리스너 설정
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}