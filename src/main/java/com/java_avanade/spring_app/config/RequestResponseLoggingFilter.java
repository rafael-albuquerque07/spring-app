package com.java_avanade.spring_app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/h2-console",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Não registre logs para algumas URLs específicas
        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Envolve request e response para poder ler seus conteúdos
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Executa o próximo filtro na cadeia
            filterChain.doFilter(requestWrapper, responseWrapper);

            // Calcula o tempo de processamento
            long duration = System.currentTimeMillis() - startTime;

            // Registra informações da requisição e resposta
            logRequestResponse(requestWrapper, responseWrapper, duration);
        } finally {
            // Garante que o conteúdo da resposta ainda seja enviado ao cliente
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean isExcludedPath(String requestURI) {
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    private void logRequestResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String queryString = request.getQueryString();
        String path = queryString != null ? request.getRequestURI() + "?" + queryString : request.getRequestURI();

        int status = response.getStatus();

        logger.info("{} {} {} - {}ms", request.getMethod(), path, status, duration);

        // Se houver erro, registre mais detalhes
        if (status >= 400) {
            logger.debug("Request Headers: {}", getHeadersAsString(request));

            // Para evitar logging de dados sensíveis, podemos omitir o corpo em rotas de autenticação
            if (!path.contains("/auth/login") && !path.contains("/auth/register")) {
                logger.debug("Request Body: {}", getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding()));
            }
        }
    }

    private String getHeadersAsString(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .filter(header -> !header.equalsIgnoreCase("authorization") &&
                        !header.equalsIgnoreCase("cookie"))
                .map(header -> header + ":" + Collections.list(request.getHeaders(header)))
                .reduce((header1, header2) -> header1 + ", " + header2)
                .orElse("");
    }

    private String getContentAsString(byte[] content, String encoding) {
        if (content.length == 0) return "";
        try {
            return new String(content, encoding);
        } catch (UnsupportedEncodingException e) {
            return "Unsupported encoding";
        }
    }
}