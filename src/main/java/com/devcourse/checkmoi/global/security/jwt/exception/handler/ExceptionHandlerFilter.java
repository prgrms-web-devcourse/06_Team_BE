package com.devcourse.checkmoi.global.security.jwt.exception.handler;

import com.devcourse.checkmoi.global.exception.error.ErrorResponse;
import com.devcourse.checkmoi.global.security.jwt.exception.TokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (TokenException e) {
            log.info("exception handler token error : {}", e.getErrorMessage());
            generateErrorResponse(response, e);
        }
    }

    private void generateErrorResponse(HttpServletResponse response, TokenException e)
        throws IOException {
        response.setStatus(e.getErrorMessage().getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
            .write(objectMapper.writeValueAsString(ErrorResponse.of(e.getErrorMessage())));
    }

}
