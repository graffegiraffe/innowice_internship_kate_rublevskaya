package by.rublevskaya.userservice.mapper;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import by.rublevskaya.userservice.entity.CardInfo;
import by.rublevskaya.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "user", source = "userId")
    CardInfo toEntity(CardRequest cardRequest);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(cardInfo.getUser().getName() + \" \" + cardInfo.getUser().getSurname())")
    CardResponse toResponse(CardInfo cardInfo);

    default User mapUserIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(CardRequest cardRequest, @MappingTarget CardInfo cardInfo);
}
