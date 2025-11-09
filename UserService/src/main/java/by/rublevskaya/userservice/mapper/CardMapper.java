package by.rublevskaya.userservice.mapper;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import by.rublevskaya.userservice.entity.CardInfo;
import by.rublevskaya.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "user", source = "userId", qualifiedByName = "mapUserIdToUser")
    @Mapping(target = "id", ignore = true)
    CardInfo toEntity(CardRequest cardRequest);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user", qualifiedByName = "getUserFullName")
    CardResponse toResponse(CardInfo cardInfo);

    @Named("mapUserIdToUser")
    default User mapUserIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    @Named("getUserFullName")
    default String getUserFullName(User user) {
        if (user == null) {
            return null;
        }
        String name = user.getName() != null ? user.getName() : "";
        String surname = user.getSurname() != null ? user.getSurname() : "";
        return (name + " " + surname).trim();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(CardRequest cardRequest, @MappingTarget CardInfo cardInfo);
}