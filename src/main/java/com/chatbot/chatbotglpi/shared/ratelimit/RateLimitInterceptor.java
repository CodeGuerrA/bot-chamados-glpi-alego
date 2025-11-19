package com.chatbot.chatbotglpi.shared.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor para aplicar rate limiting baseado em IP.
 *
 * Protege endpoints públicos contra abuso e ataques de DoS.
 *
 * Usa Resilience4j RateLimiter com chave por IP do cliente.
 *
 * Configuração:
 * - Limite padrão: 100 requisições por minuto por IP
 * - Webhooks: 30 requisições por minuto por IP
 * - Health checks: 10 requisições por minuto por IP
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Cache de rate limiters por chave (IP + tipo de endpoint)
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        // Cria chave única: path-type:IP
        String key = getPathType(path) + ":" + clientIp;

        try {
            // Obtém ou cria rate limiter para esta chave
            RateLimiter rateLimiter = rateLimiters.computeIfAbsent(key, k -> {
                RateLimiterConfig config = getRateLimiterConfig(path);
                return RateLimiter.of(k, config);
            });

            // Tenta adquirir permissão
            rateLimiter.acquirePermission();

            return true; // Permitido

        } catch (RequestNotPermitted e) {
            // Rate limit excedido
            log.warn("Rate limit excedido - IP: {} | Path: {} | Type: {}",
                     clientIp, path, getPathType(path));

            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\",\"clientIp\":\"%s\"}",
                    clientIp
            ));

            return false; // Bloqueado
        }
    }

    /**
     * Retorna a configuração de rate limit apropriada para o path
     */
    private RateLimiterConfig getRateLimiterConfig(String path) {
        if (path.startsWith("/api/webhook/")) {
            // Webhooks: 30 req/min
            return RateLimiterConfig.custom()
                    .limitForPeriod(30)
                    .limitRefreshPeriod(Duration.ofSeconds(60))
                    .timeoutDuration(Duration.ofSeconds(0))
                    .build();
        } else if (path.contains("/health") || path.contains("/actuator")) {
            // Health checks: 10 req/min
            return RateLimiterConfig.custom()
                    .limitForPeriod(10)
                    .limitRefreshPeriod(Duration.ofSeconds(60))
                    .timeoutDuration(Duration.ofSeconds(0))
                    .build();
        } else {
            // Padrão: 100 req/min
            return RateLimiterConfig.custom()
                    .limitForPeriod(100)
                    .limitRefreshPeriod(Duration.ofSeconds(60))
                    .timeoutDuration(Duration.ofSeconds(0))
                    .build();
        }
    }

    /**
     * Retorna o tipo de path para logging
     */
    private String getPathType(String path) {
        if (path.startsWith("/api/webhook/")) {
            return "webhook";
        } else if (path.contains("/health") || path.contains("/actuator")) {
            return "health";
        } else {
            return "default";
        }
    }

    /**
     * Extrai IP real do cliente (considerando proxies/load balancers)
     */
    private String getClientIp(HttpServletRequest request) {
        // Headers comuns de proxies
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_CLIENT_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode conter múltiplos IPs separados por vírgula
                // Pega o primeiro (cliente original)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback: IP direto da conexão
        return request.getRemoteAddr();
    }

    /**
     * Determina qual configuração de rate limit usar baseado no path
     */
    private String getRateLimiterName(String path) {
        if (path.startsWith("/api/webhook/")) {
            return "webhook";
        } else if (path.contains("/health") || path.contains("/actuator")) {
            return "health";
        } else {
            return "default";
        }
    }
}
