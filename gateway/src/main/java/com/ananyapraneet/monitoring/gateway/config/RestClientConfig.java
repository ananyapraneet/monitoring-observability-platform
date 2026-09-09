package com.ananyapraneet.monitoring.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {

                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {
                        HttpServletRequest currentRequest = attributes.getRequest();

                        String correlationId =
                                currentRequest.getHeader(CORRELATION_ID_HEADER);

                        if (correlationId != null && !correlationId.isBlank()) {
                            request.getHeaders().set(
                                    CORRELATION_ID_HEADER,
                                    correlationId
                            );
                        }
                    }

                    return execution.execute(request, body);
                });
    }
}
