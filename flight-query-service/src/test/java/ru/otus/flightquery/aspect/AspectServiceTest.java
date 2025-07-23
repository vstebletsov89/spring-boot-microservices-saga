package ru.otus.flightquery.aspect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        AspectService.class,
        ExecutionTimeLogger.class})
@ExtendWith(OutputCaptureExtension.class)
@EnableAspectJAutoProxy
class AspectServiceTest {

    @Autowired
    private AspectService aspectService;

    @Test
    void shouldLogExecutionTime(CapturedOutput output) throws Exception {
        aspectService.doWork();

        assertThat(output.getOut()).contains("executed in");
    }
}
