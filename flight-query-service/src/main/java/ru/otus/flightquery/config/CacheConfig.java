package ru.otus.flightquery.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class CacheConfig {

    public static final String ROUND_TRIP_FLIGHTS = "roundTripFlights";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                ROUND_TRIP_FLIGHTS
        );
        manager.setCaffeine(defaultCacheSpec());
        manager.setAllowNullValues(false);
        return manager;
    }


    private Caffeine<Object, Object> defaultCacheSpec() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats();
    }


    @Bean
    public MeterBinder cacheMetrics(CacheManager cacheManager) {
        return registry -> {
            if (cacheManager instanceof CaffeineCacheManager caffeineManager) {
                var cacheNames = caffeineManager.getCacheNames().stream().toList();

                for (String name : cacheNames) {

                    CaffeineCache cache = (CaffeineCache) caffeineManager.getCache(name);

                    if (cache != null) {
                        registry.gauge("cache_size", Tags.of("cache", name), cache, c -> c.getNativeCache().estimatedSize());
                        registry.gauge("cache_requests_hit", Tags.of("cache", name), cache, c -> c.getNativeCache().stats().hitCount());
                        registry.gauge("cache_requests_miss", Tags.of("cache", name), cache, c -> c.getNativeCache().stats().missCount());
                    }
                }
                log.info("Cache metrics registered for caches: {}", cacheNames);
            }
        };
    }
}
