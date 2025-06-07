package ru.otus.auth.service;

import com.google.common.hash.BloomFilter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.auth.offheap.BloomFilterProperties;
import ru.otus.auth.offheap.MappedByteBufferStorageImpl;
import ru.otus.auth.offheap.OffHeapStorage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;


@Service
@Slf4j
public class BloomFilterManager {

    private final BloomFilterProperties properties;
    private final OffHeapStorage storage;
    private BloomFilter<byte[]> bloomFilter;

    public BloomFilterManager(BloomFilterProperties properties) {
        this.properties = properties;
        this.storage = new MappedByteBufferStorageImpl(properties.getBufferSize());
    }

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Bloom Filter");
            storage.loadFile(properties.getFilePath());
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(storage.readBytes()))) {
                this.bloomFilter = (BloomFilter<byte[]>) ois.readObject();
            }
            log.info("Bloom filter loaded successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Bloom filter", e);
        }
    }

    public BloomFilter<byte[]> getFilter() {
        return bloomFilter;
    }
}
