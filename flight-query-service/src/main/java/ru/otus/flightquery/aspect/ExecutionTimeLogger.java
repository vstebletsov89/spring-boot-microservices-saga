package ru.otus.flightquery.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.otus.flightquery.annotation.LogExecutionTime;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLogger {

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long startTime = System.nanoTime();
        log.info("Aspect called");

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("Exception in method {}: {}", joinPoint.toShortString(), ex.getMessage(), ex);
            throw ex;
        }

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("{} executed in {} ms", joinPoint.toShortString(), durationMs);

        return result;
    }
}
