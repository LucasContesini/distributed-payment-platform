package com.finflow.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var start = System.currentTimeMillis();

        return chain.filter(exchange).doFinally(signal -> {
            var status = exchange.getResponse().getStatusCode();
            var elapsed = System.currentTimeMillis() - start;
            log.info("method={} path={} status={} elapsed={}ms",
                    request.getMethod(),
                    request.getPath().value(),
                    status != null ? status.value() : "unknown",
                    elapsed);
        });
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
