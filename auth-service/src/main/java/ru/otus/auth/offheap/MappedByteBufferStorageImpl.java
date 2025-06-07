package ru.otus.auth.offheap;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MappedByteBufferStorageImpl implements OffHeapStorage {
    private MappedByteBuffer mappedByteBuffer;
    private final int size;

    public MappedByteBufferStorageImpl(int size) {
        this.size = size;
    }

    @Override
    public void loadFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        }
    }


    @Override
    public byte[] readBytes() {
        if (mappedByteBuffer == null || !mappedByteBuffer.hasRemaining()) {
            throw new RuntimeException("Buffer is empty or not initialized");
        }
        mappedByteBuffer.rewind();
        byte[] bytes = new byte[mappedByteBuffer.remaining()];
        mappedByteBuffer.get(bytes);
        return bytes;
    }
}
