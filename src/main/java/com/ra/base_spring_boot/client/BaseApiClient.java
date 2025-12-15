package com.ra.base_spring_boot.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseApiClient {

    private final WebClient externalWebClient;
    private static final Duration DEFAULT_BLOCK_TIMEOUT = Duration.ofSeconds(15);

    public <T> Mono<T> get(String path, Class<T> responseType, Map<String, String> headers) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");
        return externalWebClient.get()
                .uri(path)
                .headers(h -> applyHeaders(h, headers))
                .accept(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("GET {} failed: {} - {}", path, ex.getStatusCode().value(), ex.getResponseBodyAsString()))
                ;
    }

    public <B, T> Mono<T> post(String path, B body, Class<T> responseType, Map<String, String> headers) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");
        Objects.requireNonNull(body, "body must not be null");
        return externalWebClient.post()
                .uri(path)
                .headers(h -> applyHeaders(h, headers))
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .accept(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("POST {} failed: {} - {}", path, ex.getStatusCode().value(), ex.getResponseBodyAsString()))
                ;
    }

    public <T> T getSync(String path, Class<T> responseType, Map<String, String> headers) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");
        return get(path, responseType, headers).block(DEFAULT_BLOCK_TIMEOUT);
    }

    public <B, T> T postSync(String path, B body, Class<T> responseType, Map<String, String> headers) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");
        Objects.requireNonNull(body, "body must not be null");
        return post(path, body, responseType, headers).block(DEFAULT_BLOCK_TIMEOUT);
    }

    private void applyHeaders(org.springframework.http.HttpHeaders httpHeaders, Map<String, String> headers) {
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpHeaders::add);
        }
    }
}
