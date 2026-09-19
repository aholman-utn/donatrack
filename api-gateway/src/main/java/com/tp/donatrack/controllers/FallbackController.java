package com.tp.donatrack.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);
    private final WebClient webClient;
    private final String localUrl;

    public FallbackController(
            WebClient.Builder builder,
            @Value("${app.logistica.local-url:http://localhost:8083}") String localUrl
    ) {
        this.localUrl = localUrl;
        this.webClient = builder.baseUrl(localUrl).build();
        logger.info("FallbackController initialized. Routing fallback traffic to: {}", localUrl);
    }

    @RequestMapping("/fallback/logistica/**")
    public Mono<ResponseEntity<String>> logisticaFallback(ServerWebExchange exchange) {
        String finalPath = exchange.getRequest().getHeaders().getFirst("X-Original-Path");

        if (finalPath == null || finalPath.isEmpty()) {
            logger.error("WARNING: No se pudo obtener el X-Original-Path. Fallback genérico activado.");
            finalPath = "/api/logistica";
        }

        String query = exchange.getRequest().getURI().getRawQuery();
        String finalUri = query != null ? finalPath + "?" + query : finalPath;
        HttpMethod method = exchange.getRequest().getMethod();

        logger.warn("Circuit Breaker OPEN. Cloud service unreachable.");
        logger.warn("Redirecting request [{}] to local fallback: {}{}", method, localUrl, finalUri);

        String cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);

        WebClient.RequestBodySpec request = webClient
                .method(method)
                .uri(finalUri)
                .headers(headers -> headers.addAll(exchange.getRequest().getHeaders()));

        Mono<ResponseEntity<String>> responseMono;

        if (cachedBody != null) {
            logger.debug("Attached cached request body to local fallback request.");
            responseMono = request.bodyValue(cachedBody).exchangeToMono(res -> res.toEntity(String.class));
        } else {
            logger.debug("No request body found in the original request.");
            responseMono = request.exchangeToMono(res -> res.toEntity(String.class));
        }

        return responseMono.flatMap(response -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Local fallback executed successfully with status: {}", response.getStatusCode());
                return Mono.just(response);
            } else {
                logger.error("Local fallback service also failed with status: {}. Returning Service Unavailable.", response.getStatusCode());
                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("{\"error\": \"Both cloud and local logistics services are currently unavailable.\"}"));
            }
        }).onErrorResume(Exception.class, e -> {
            logger.error("Fatal error communicating with local fallback service on port 8083: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\": \"Local fallback service is completely down or unreachable.\"}"));
        });
    }
}