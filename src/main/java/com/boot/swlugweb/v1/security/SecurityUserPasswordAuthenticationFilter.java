package com.boot.swlugweb.v1.security;

import com.boot.swlugweb.v1.login.LoginRequestDto;
import com.boot.swlugweb.v1.login.LoginResponseDto;
import com.boot.swlugweb.v1.login.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class SecurityUserPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoginService loginService;

    public SecurityUserPasswordAuthenticationFilter(AuthenticationManager authenticationManager, LoginService loginService) {
        super(authenticationManager);
        this.loginService = loginService;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            // LoginService를 통해 로그인 시도 횟수 검증
            LoginResponseDto loginResponse = loginService.authenticateUser(
                    loginRequestDto.getUserId(),
                    loginRequestDto.getPassword()
            );

            if (!loginResponse.isSuccess()) {
                throw new AuthenticationServiceException(loginResponse.getMessage());
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUserId(),
                            loginRequestDto.getPassword(),
                            Collections.emptyList()
                    )
            );
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청 처리 중 오류가 발생했습니다.", e);
        }
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        session.setAttribute("USER", userDetails.getUsername());

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        LoginResponseDto successResponse = new LoginResponseDto(
                true,
                "로그인 성공",
                userDetails.getUsername(),
                role  // role 정보 추가
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(successResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        LoginResponseDto errorResponse = new LoginResponseDto(
                false,
                failed.getMessage(),
                null,
                null  // 실패시 role은 null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}