package com.boot.swlugweb.v1.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 특정 헤더 제거
        httpResponse.setHeader("Server", ""); // 서버 정보 숨기기
        httpResponse.setHeader("X-Vercel-Id", "");
        httpResponse.setHeader("Strict-Transport-Security", "");
        httpResponse.setHeader("Vary", "");
        httpResponse.setHeader("X-Xss-Protection", ""); // 프레임워크 정보 숨기기
        httpResponse.setHeader("X-Content-Type-Options", "");
        httpResponse.setHeader("X-Frame-Options", "");
        httpResponse.setHeader("Strict-Transport-Security", ""); // 필요에 따라 제거
        httpResponse.setHeader("Age", ""); // 캐시 관련 정보 제거
        httpResponse.setHeader("Last-Modified", ""); // 캐시 관련 정보 제거

        chain.doFilter(request, response);
    }
}
