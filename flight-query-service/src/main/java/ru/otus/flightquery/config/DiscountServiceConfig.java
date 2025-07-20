package ru.otus.flightquery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;

@Configuration
public class DiscountServiceConfig {

    @Bean
    public RestClient discountRestClient() {
        return RestClient.builder()
                .baseUrl("http://discount-service:8086")
                .build();
    }

    @Bean(name = "discountExecutor")
    public Executor discountExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("DiscountService-");
        executor.initialize();
        return executor;
    }
}
