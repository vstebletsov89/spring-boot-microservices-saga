package ru.otus.auth.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.otus.auth.mapper.AuthRequestMapper;
import ru.otus.auth.mapper.AuthResponseMapper;
import ru.otus.auth.mapper.RegisterRequestMapper;
import ru.otus.auth.service.AuthService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends ru.otus.auth.grpc.AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private final AuthRequestMapper authRequestMapper;
    private final AuthResponseMapper authResponseMapper;
    private final RegisterRequestMapper registerRequestMapper;

    @Override
    public void register(ru.otus.auth.grpc.RegisterRequestGrpc request, StreamObserver<ru.otus.auth.grpc.AuthResponseGrpc> responseObserver) {
        try {

            var response =
                    authService.register(registerRequestMapper.fromGrpc(request));

            responseObserver.onNext(authResponseMapper.toGrpc(response));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void login(ru.otus.auth.grpc.AuthRequestGrpc request, StreamObserver<ru.otus.auth.grpc.AuthResponseGrpc> responseObserver) {
        try {

            var response =
                    authService.login(authRequestMapper.fromGrpc(request));

            responseObserver.onNext(authResponseMapper.toGrpc(response));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void refresh(ru.otus.auth.grpc.RefreshRequestGrpc request, StreamObserver<ru.otus.auth.grpc.AuthResponseGrpc> responseObserver) {
        try {
            var response = authService.refresh(request.getRefreshToken());

            responseObserver.onNext(authResponseMapper.toGrpc(response));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}