package com.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        log.info("[GATEWAY-REQ] {} {} [CorrelationId: {}]", method, path, correlationId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long start = exchange.getAttribute(START_TIME_ATTR);
            long latency = (start != null) ? (System.currentTimeMillis() - start) : 0;
            HttpStatusCode status = exchange.getResponse().getStatusCode();

            log.info("[GATEWAY-RESP] {} {} -> {} ({}ms) [CorrelationId: {}]",
                    method, path, status != null ? status.value() : "UNKNOWN", latency, correlationId);
        }));
    }

    @Override
    public int getOrder() {
        return 1; // Run after correlation ID filter
    }
}
