package com.ra.base_spring_boot.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalApiProperties.class)
public class WebClientConfig {

    private final ExternalApiProperties props;  // 🔸 Inject cấu hình từ application.yml

    @Bean
    public WebClient externalWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeout())   //   Timeout kết nối
                .responseTimeout(Duration.ofMillis(props.getReadTimeout())) //  
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(props.getReadTimeout(), TimeUnit.MILLISECONDS))
                );

        String baseUrl = Objects.requireNonNull(props.getBaseUrl(), "external.api.baseUrl must not be null");
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(Objects.requireNonNull(httpClient)))
                .filter(Objects.requireNonNull(logRequest()))
                .filter(Objects.requireNonNull(logResponse()))
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("[WebClient] {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) -> values.forEach(v -> log.debug("[WebClient][H] {}: {}", name, v)));
            }
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("[WebClient] <== {}", clientResponse.statusCode());
                clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(v -> log.debug("[WebClient][H] {}: {}", name, v)));
            }
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}
