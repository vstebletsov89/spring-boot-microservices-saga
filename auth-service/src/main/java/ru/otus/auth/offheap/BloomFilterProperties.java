package ru.otus.auth.offheap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bloom-filter")
public class BloomFilterProperties {

    private String filePath;
    private int bufferSize;
    private double falsePositiveRate;
    private int expectedInsertions;
}
