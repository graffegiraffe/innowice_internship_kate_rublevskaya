package by.rublevskaya.userservice.service;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardInfoService {
    CardResponse createCard(CardRequest cardRequest);
    CardResponse getCardById(Long id);
    Page<CardResponse> getAllCards(Pageable pageable);
    List<CardResponse> getCardsByUserId(Long userId);
    CardResponse updateCard(Long id, CardRequest cardRequest);
    void deleteCard(Long id);
}