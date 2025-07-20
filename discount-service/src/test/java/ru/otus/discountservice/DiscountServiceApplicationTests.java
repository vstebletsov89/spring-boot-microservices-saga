package ru.otus.discountservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscountServiceApplicationTests {

    //TODO: check calling discount-service from flight-query-service
    //TODO: add aop to measure time of methods in ms for flight-query-service calling discount service

    //TODO: add notification service using transactional outbox and completebaleFuture's


    @Test
    void contextLoads() {
    }

}
