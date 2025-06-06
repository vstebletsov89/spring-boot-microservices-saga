package ru.otus.auth.mapper;

import org.mapstruct.Mapper;
import ru.otus.auth.dto.AuthResponse;
import ru.otus.auth.grpc.AuthResponseGrpc;

@Mapper(componentModel = "spring")
public interface AuthResponseMapper {

    AuthResponseGrpc toGrpc(AuthResponse response);
}
