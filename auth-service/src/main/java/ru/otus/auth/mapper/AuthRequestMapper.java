package ru.otus.auth.mapper;

import org.mapstruct.Mapper;
import ru.otus.auth.dto.AuthRequest;
import ru.otus.auth.grpc.AuthRequestGrpc;

@Mapper(componentModel = "spring")
public interface AuthRequestMapper {

    AuthRequest fromGrpc(AuthRequestGrpc grpcRequest);
}
