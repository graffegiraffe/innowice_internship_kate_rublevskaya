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
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final CacheManager cacheManager;

    private static final String CARD_CACHE = "cards";
    private static final String CARDS_PAGE_CACHE = "cardsPage";
    private static final String USER_CARDS_CACHE = "userCards";

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CARDS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = USER_CARDS_CACHE, key = "#cardRequest.userId")
    })
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
    @Cacheable(value = CARD_CACHE, key = "#id")
    public CardResponse getCardById(Long id) {
        log.info("Fetching card by id: {}", id);
        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toResponse(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CARDS_PAGE_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<CardResponse> getAllCards(Pageable pageable) {
        log.info("Fetching all cards with pagination: {}", pageable);
        return cardInfoRepository.findAll(pageable)
                .map(cardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CARDS_CACHE, key = "#userId")
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
    @Caching(evict = {
            @CacheEvict(value = CARDS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = USER_CARDS_CACHE, allEntries = true)
    }, put = {
            @CachePut(value = CARD_CACHE, key = "#id")
    })
    public CardResponse updateCard(Long id, CardRequest cardRequest) {
        log.info("Updating card with id: {}", id);
        CardInfo existingCard = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!existingCard.getUser().getId().equals(cardRequest.getUserId())) {
            evictUserCardsCache(existingCard.getUser().getId());
        }
        if (!existingCard.getNumber().equals(cardRequest.getNumber()) &&
                cardInfoRepository.existsByNumberAndUserId(cardRequest.getNumber(), cardRequest.getUserId())) {
            throw new CardNumberExistsException(cardRequest.getNumber());
        }

        cardMapper.updateEntityFromRequest(cardRequest, existingCard);

        if (!existingCard.getUser().getId().equals(cardRequest.getUserId())) {
            User newUser = userRepository.findById(cardRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(cardRequest.getUserId()));
            existingCard.setUser(newUser);
        }

        CardInfo updatedCard = cardInfoRepository.save(existingCard);
        log.info("Card updated successfully with id: {}", id);
        return cardMapper.toResponse(updatedCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CARD_CACHE, key = "#id"),
            @CacheEvict(value = CARDS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = USER_CARDS_CACHE, allEntries = true)
    })
    public void deleteCard(Long id) {
        log.info("Deleting card with id: {}", id);
        CardInfo card = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        evictUserCardsCache(card.getUser().getId());
        cardInfoRepository.deleteById(id);
        log.info("Card deleted successfully with id: {}", id);
    }

    private void evictUserCardsCache(Long userId) {
        try {
            var cache = cacheManager.getCache(USER_CARDS_CACHE);
            if (cache != null) {
                cache.evict(userId);
                log.debug("Evicted cards cache for user id: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cards cache for user id: {}", userId, e);
        }
    }
}