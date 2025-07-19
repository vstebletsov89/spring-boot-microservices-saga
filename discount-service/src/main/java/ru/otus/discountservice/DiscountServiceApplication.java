package ru.otus.discountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.axonframework.springboot.autoconfig.AvroSerializerAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonDbSchedulerAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonJobRunrAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonServerActuatorAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonServerBusAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonTimeoutAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.AxonTracingAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.CBORMapperAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.InfraConfiguration.class,
        org.axonframework.springboot.autoconfig.InterceptorAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.JdbcAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.JpaAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.MetricsAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.MicrometerMetricsAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.NoOpTransactionAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.ObjectMapperAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.OpenTelemetryAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.SecurityAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.TransactionAutoConfiguration.class,
        org.axonframework.springboot.autoconfig.XStreamAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
public class DiscountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscountServiceApplication.class, args);
    }

}
