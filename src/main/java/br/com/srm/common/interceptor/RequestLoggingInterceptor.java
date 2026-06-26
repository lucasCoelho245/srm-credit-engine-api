package br.com.srm.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor HTTP que registra método, URL, status e tempo de resposta de cada requisição.
 *
 * Implementa HandlerInterceptor do Spring MVC com dois pontos de intercepção:
 *   - preHandle: salva o timestamp antes de o controller processar a requisição
 *   - afterCompletion: calcula o tempo decorrido e loga o resultado após a resposta
 *
 * Centraliza o logging em um único lugar — sem esse interceptor, seria necessário
 * adicionar log.info manualmente em cada método de cada controller.
 * Registrado via WebConfig para atuar apenas nas rotas /api/**.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        log.debug("→ {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long elapsed = startTime != null ? System.currentTimeMillis() - startTime : -1;
        log.debug("← {} {} [{}] {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                elapsed);
        if (ex != null) {
            log.error("Exceção não tratada em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }
}
