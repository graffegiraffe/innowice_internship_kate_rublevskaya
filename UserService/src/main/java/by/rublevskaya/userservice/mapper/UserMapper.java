package by.rublevskaya.userservice.mapper;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequest userRequest);
    UserResponse toResponse(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateEntityFromRequest(UserRequest userRequest, @MappingTarget User user);
}
