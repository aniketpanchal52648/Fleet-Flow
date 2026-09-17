package com.example.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @Test
    @DisplayName("Should generate new X-Correlation-ID when header is missing")
    void testGenerateCorrelationIdWhenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/web/v1/user-service/user")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        ServerWebExchange captured = captor.getValue();
        String reqCorrelationId = captured.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        String respCorrelationId = exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        assertNotNull(reqCorrelationId);
        assertEquals(reqCorrelationId, respCorrelationId);
    }

    @Test
    @DisplayName("Should propagate existing X-Correlation-ID from client request")
    void testPropagateExistingCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/web/v1/user-service/user")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "custom-trace-12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        ServerWebExchange captured = captor.getValue();
        assertEquals("custom-trace-12345", captured.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertEquals("custom-trace-12345", exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }
}
