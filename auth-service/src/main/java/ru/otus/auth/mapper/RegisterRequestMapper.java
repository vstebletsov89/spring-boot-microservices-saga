package ru.otus.auth.mapper;

import org.mapstruct.Mapper;
import ru.otus.auth.dto.RegisterRequest;
import ru.otus.auth.grpc.RegisterRequestGrpc;

@Mapper(componentModel = "spring")
public interface RegisterRequestMapper {

    RegisterRequest fromGrpc(RegisterRequestGrpc grpc);
}
