package ru.otus.auth.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AuthGrpcServiceTest {

    private static ManagedChannel channel;
    private static ru.otus.auth.grpc.AuthServiceGrpc.AuthServiceBlockingStub stub;

    @BeforeAll
    static void setUp() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        stub = ru.otus.auth.grpc.AuthServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void tearDown() {
        channel.shutdownNow();
    }

    @Test
    void shouldRegisterUser() {
        var request = ru.otus.auth.grpc.RegisterRequestGrpc.newBuilder()
                .setUsername("user1")
                .setPassword("pass1")
                .build();

        ru.otus.auth.grpc.AuthResponseGrpc response = stub.register(request);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void shouldLoginUser() {
        var request = ru.otus.auth.grpc.RegisterRequestGrpc.newBuilder()
                .setUsername("user2")
                .setPassword("pass2")
                .build();

       stub.register(request);

        var loginRequest = ru.otus.auth.grpc.AuthRequestGrpc.newBuilder()
                .setUsername("user2")
                .setPassword("pass2")
                .build();

        ru.otus.auth.grpc.AuthResponseGrpc loginResponse = stub.login(loginRequest);

        assertThat(loginResponse.getAccessToken()).isNotBlank();
        assertThat(loginResponse.getRefreshToken()).isNotBlank();
    }

    @Test
    void shouldRefreshToken() {
        var request = ru.otus.auth.grpc.RegisterRequestGrpc.newBuilder()
                .setUsername("user3")
                .setPassword("pass3")
                .build();

        stub.register(request);

        var loginRequest = ru.otus.auth.grpc.AuthRequestGrpc.newBuilder()
                .setUsername("user3")
                .setPassword("pass3")
                .build();

        var loginResponse = stub.login(loginRequest);

        var refreshRequest = ru.otus.auth.grpc.RefreshRequestGrpc.newBuilder()
                .setRefreshToken(loginResponse.getRefreshToken())
                .build();

        var refreshed = stub.refresh(refreshRequest);

        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotBlank();
    }
}