package com.example.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalRouteProtectionFilterTest {

    private InternalRouteProtectionFilter filter;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new InternalRouteProtectionFilter();
    }

    @Test
    @DisplayName("Should block external access to /internal endpoints with 403 Forbidden")
    void testBlockInternalEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/internal/v1/tracking-service/nearby-drivers")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should allow requests with public /web/v1 paths to proceed")
    void testAllowPublicEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/web/v1/shipment-service/shipments")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }
}
