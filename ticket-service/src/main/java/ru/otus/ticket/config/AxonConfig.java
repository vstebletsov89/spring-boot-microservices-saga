package ru.otus.ticket.config;

import com.thoughtworks.xstream.security.AnyTypePermission;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.axonframework.springboot.util.jpa.ContainerManagedEntityManagerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Bean
    public EntityManagerProvider entityManagerProvider() {
        return new ContainerManagedEntityManagerProvider();
    }

    @Bean
    public Serializer tokenSerializer() {
        return XStreamSerializer.builder()
                .lenientDeserialization()
                .disableAxonTypeSecurity()
                .build();
    }

    @Bean
    public TokenStore tokenStore(EntityManagerProvider entityManagerProvider, Serializer tokenSerializer) {
        return JpaTokenStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(tokenSerializer)
                .build();
    }
}
