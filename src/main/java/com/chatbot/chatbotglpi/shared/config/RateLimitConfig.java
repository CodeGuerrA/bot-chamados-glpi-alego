package com.chatbot.chatbotglpi.shared.config;

import com.chatbot.chatbotglpi.shared.ratelimit.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de Rate Limiting para endpoints HTTP.
 *
 * Aplica interceptor de rate limiting em todas as requisições.
 *
 * Proteção por camadas:
 * 1. API Gateway (se existir) - rate limit global
 * 2. RateLimitInterceptor - rate limit por IP (gerenciamento interno)
 * 3. Resilience4j no código - rate limit por operação
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/webhook/**",      // Webhooks (maior risco)
                        "/api/**"               // Todas as APIs
                )
                .excludePathPatterns(
                        "/actuator/**",         // Monitoramento interno (sem rate limit)
                        "/error"                // Página de erro
                );
    }
}
