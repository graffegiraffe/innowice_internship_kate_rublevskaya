package by.rublevskaya.userservice.mapper;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.CardInfo;
import by.rublevskaya.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequest userRequest);

    @Mapping(target = "cards", ignore = true)
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateEntityFromRequest(UserRequest userRequest, @MappingTarget User user);

    default UserResponse toResponseWithCards(User user) {
        UserResponse response = toResponse(user);
        if (user.getCards() != null && !user.getCards().isEmpty()) {
            List<UserResponse.CardSummary> cardSummaries = user.getCards().stream()
                    .map(this::toCardSummary)
                    .collect(Collectors.toList());
            response.setCards(cardSummaries);
        }
        return response;
    }

    default UserResponse.CardSummary toCardSummary(CardInfo cardInfo) {
        UserResponse.CardSummary summary = new UserResponse.CardSummary();
        summary.setId(cardInfo.getId());
        summary.setNumber(cardInfo.getNumber());
        summary.setHolder(cardInfo.getHolder());
        summary.setExpirationDate(cardInfo.getExpirationDate());
        return summary;
    }
}