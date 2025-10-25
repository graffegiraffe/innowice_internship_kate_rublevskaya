package by.rublevskaya.userservice.service.impl;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import by.rublevskaya.userservice.entity.CardInfo;
import by.rublevskaya.userservice.entity.User;
import by.rublevskaya.userservice.exception.CardNotFoundException;
import by.rublevskaya.userservice.exception.CardNumberExistsException;
import by.rublevskaya.userservice.exception.UserNotFoundException;
import by.rublevskaya.userservice.mapper.CardMapper;
import by.rublevskaya.userservice.repository.CardInfoRepository;
import by.rublevskaya.userservice.repository.UserRepository;
import by.rublevskaya.userservice.service.CardInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CardInfoServiceImpl implements CardInfoService {

    private final CardInfoRepository cardInfoRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        log.info("Creating new card for user id: {}", cardRequest.getUserId());

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException(cardRequest.getUserId()));

        if (cardInfoRepository.existsByNumberAndUserId(cardRequest.getNumber(), user.getId())) {
            throw new CardNumberExistsException(cardRequest.getNumber());
        }

        CardInfo cardInfo = cardMapper.toEntity(cardRequest);
        cardInfo.setUser(user);
        CardInfo savedCard = cardInfoRepository.save(cardInfo);

        log.info("Card created successfully with id: {}", savedCard.getId());
        return cardMapper.toResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        log.info("Fetching card by id: {}", id);

        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        return cardMapper.toResponse(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(Pageable pageable) {
        log.info("Fetching all cards with pagination: {}", pageable);

        return cardInfoRepository.findAll(pageable)
                .map(cardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByUserId(Long userId) {
        log.info("Fetching cards for user id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return cardInfoRepository.findCardsByUserId(userId).stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long id, CardRequest cardRequest) {
        log.info("Updating card with id: {}", id);

        CardInfo existingCard = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!existingCard.getNumber().equals(cardRequest.getNumber()) &&
                cardInfoRepository.existsByNumberAndUserId(cardRequest.getNumber(), existingCard.getUser().getId())) {
            throw new CardNumberExistsException(cardRequest.getNumber());
        }

        cardMapper.updateEntityFromRequest(cardRequest, existingCard);
        CardInfo updatedCard = cardInfoRepository.save(existingCard);

        log.info("Card updated successfully with id: {}", id);
        return cardMapper.toResponse(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        log.info("Deleting card with id: {}", id);

        if (!cardInfoRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }

        cardInfoRepository.deleteById(id);
        log.info("Card deleted successfully with id: {}", id);
    }
}