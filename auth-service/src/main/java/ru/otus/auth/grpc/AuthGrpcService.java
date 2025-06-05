package ru.otus.auth.grpc;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.otus.auth.service.AuthService;


@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceGrpcImplBase {

    private final AuthService authService;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponseGrpc> responseObserver) {
        try {
            //TODO: add mapstruct
            RegisterRequest domainRequest = new RegisterRequest(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );

            AuthResponse response = authService.register(domainRequest);

            AuthResponseGrpc grpcResponse = AuthResponseGrpc.newBuilder()
                    .setAccessToken(response.accessToken())
                    .setRefreshToken(response.refreshToken())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void login(AuthRequest request, StreamObserver<AuthResponseGrpc> responseObserver) {
        try {
            AuthRequest domainRequest = new AuthRequest(
                    request.getUsername(),
                    request.getPassword()
            );

            AuthResponse response = authService.login(domainRequest);

            AuthResponseGrpc grpcResponse = AuthResponseGrpc.newBuilder()
                    .setAccessToken(response.accessToken())
                    .setRefreshToken(response.refreshToken())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void refresh(RefreshRequest request, StreamObserver<AuthResponseGrpc> responseObserver) {
        try {
            AuthResponse response = authService.refresh(request.getRefreshToken());

            AuthResponseGrpc grpcResponse = AuthResponseGrpc.newBuilder()
                    .setAccessToken(response.accessToken())
                    .setRefreshToken(response.refreshToken())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
