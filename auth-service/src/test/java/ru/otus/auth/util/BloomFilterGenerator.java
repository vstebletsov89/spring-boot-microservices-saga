package ru.otus.auth.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static ru.otus.auth.util.PasswordHashUtil.hashPassword;

public class BloomFilterGenerator {
    public static void main(String[] args) throws Exception {

        List<String> compromised = List.of(
                "123456",
                "123456789",
                "1234",
                "12345678",
                "12345",
                "password",
                "111111",
                "admin",
                "123123",
                "abc123"
        );

        BloomFilter<byte[]> filter = BloomFilter.create(
                Funnels.byteArrayFunnel(),
                10_000,
                0.01
        );

        for (String pw : compromised) {
            filter.put(hashPassword(pw, "SHA-256"));
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("compromised.bin"))) {
            oos.writeObject(filter);
        }

        System.out.println("Bloom filter saved to compromised.bin");
    }
}
