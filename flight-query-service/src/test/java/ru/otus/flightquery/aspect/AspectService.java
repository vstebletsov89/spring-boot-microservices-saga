package ru.otus.flightquery.aspect;

import org.springframework.stereotype.Component;
import ru.otus.flightquery.annotation.LogExecutionTime;

@Component
public class AspectService {

    @LogExecutionTime
    public void doWork() throws InterruptedException {
        Thread.sleep(30);
    }
}
