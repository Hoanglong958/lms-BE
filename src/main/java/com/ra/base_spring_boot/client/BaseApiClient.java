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

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseApiClient {

    private final WebClient externalWebClient;
    private static final Duration DEFAULT_BLOCK_TIMEOUT = Duration.ofSeconds(15);

    public <T> Mono<T> get(String path, Class<T> responseType, Map<String, String> headers) {
        return externalWebClient.get()
                .uri(path)
                .headers(h -> applyHeaders(h, headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("GET {} failed: {} - {}", path, ex.getRawStatusCode(), ex.getResponseBodyAsString()))
                ;
    }

    public <B, T> Mono<T> post(String path, B body, Class<T> responseType, Map<String, String> headers) {
        return externalWebClient.post()
                .uri(path)
                .headers(h -> applyHeaders(h, headers))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("POST {} failed: {} - {}", path, ex.getRawStatusCode(), ex.getResponseBodyAsString()))
                ;
    }

    public <T> T getSync(String path, Class<T> responseType, Map<String, String> headers) {
        return get(path, responseType, headers).block(DEFAULT_BLOCK_TIMEOUT);
    }

    public <B, T> T postSync(String path, B body, Class<T> responseType, Map<String, String> headers) {
        return post(path, body, responseType, headers).block(DEFAULT_BLOCK_TIMEOUT);
    }

    private void applyHeaders(org.springframework.http.HttpHeaders httpHeaders, Map<String, String> headers) {
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpHeaders::add);
        }
    }
}
