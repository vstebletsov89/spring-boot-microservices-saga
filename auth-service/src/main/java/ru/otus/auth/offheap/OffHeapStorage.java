package ru.otus.auth.offheap;

import java.io.IOException;

public interface OffHeapStorage {
    void loadFile(String filePath) throws IOException;
    byte[] readBytes();
}
