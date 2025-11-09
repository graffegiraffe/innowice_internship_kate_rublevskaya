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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardInfoServiceImplTest {

    @Mock
    private CardInfoRepository cardInfoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private CardInfoServiceImpl cardInfoService;

    private CardRequest cardRequest;
    private CardInfo cardInfo;
    private CardResponse cardResponse;
    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Kater");
        user.setSurname("Rublevsk");

        cardRequest = new CardRequest();
        cardRequest.setUserId(1L);
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("Kate Rublevskaya");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(1));

        cardInfo = new CardInfo();
        cardInfo.setId(1L);
        cardInfo.setUser(user);
        cardInfo.setNumber("1234567890123456");
        cardInfo.setHolder("Kate Rublevskaya");
        cardInfo.setExpirationDate(LocalDate.now().plusYears(1));

        cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setNumber("1234567890123456");
        cardResponse.setHolder("JKate Rublevskaya");
        cardResponse.setExpirationDate(LocalDate.now().plusYears(1));
        cardResponse.setUserId(1L);
        cardResponse.setUserName("Kate Rublevskaya");

        pageable = PageRequest.of(0, 10, Sort.by("id"));
        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @Test
    void createCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardInfoRepository.existsByNumberAndUserId(cardRequest.getNumber(), 1L)).thenReturn(false);
        when(cardMapper.toEntity(cardRequest)).thenReturn(cardInfo);
        when(cardInfoRepository.save(any(CardInfo.class))).thenReturn(cardInfo);
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);

        CardResponse result = cardInfoService.createCard(cardRequest);
        assertNotNull(result);
        assertEquals(cardResponse, result);
        verify(cardInfoRepository).save(any(CardInfo.class));
    }

    @Test
    void createCardUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> cardInfoService.createCard(cardRequest));
    }

    @Test
    void createCardCardNumberExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardInfoRepository.existsByNumberAndUserId(cardRequest.getNumber(), 1L)).thenReturn(true);
        assertThrows(CardNumberExistsException.class, () -> cardInfoService.createCard(cardRequest));
    }

    @Test
    void getCardById() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);
        CardResponse result = cardInfoService.getCardById(1L);
        assertNotNull(result);
        assertEquals(cardResponse, result);
    }

    @Test
    void getCardByIdNotFound() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardInfoService.getCardById(1L));
    }

    @Test
    void getAllCards() {
        Page<CardInfo> cardPage = new PageImpl<>(List.of(cardInfo));
        when(cardInfoRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);
        Page<CardResponse> result = cardInfoService.getAllCards(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(cardResponse, result.getContent().get(0));
    }

    @Test
    void getCardsByUserId() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardInfoRepository.findCardsByUserId(1L)).thenReturn(List.of(cardInfo));
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);

        List<CardResponse> result = cardInfoService.getCardsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cardResponse, result.get(0));
    }

    @Test
    void getCardsByUserIdUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> cardInfoService.getCardsByUserId(1L));
    }

    @Test
    void updateCard() {
        CardRequest updateRequest = new CardRequest();
        updateRequest.setUserId(1L);
        updateRequest.setNumber("9876543210987654");

        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.existsByNumberAndUserId(updateRequest.getNumber(), 1L)).thenReturn(false);
        doNothing().when(cardMapper).updateEntityFromRequest(updateRequest, cardInfo);
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);

        CardResponse result = cardInfoService.updateCard(1L, updateRequest);
        assertNotNull(result);
        verify(cardInfoRepository).save(cardInfo);
    }

    @Test
    void updateCardNotFound() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardInfoService.updateCard(1L, cardRequest));
    }

    @Test
    void updateCardCardNumberExists() {
        CardRequest updateRequest = new CardRequest();
        updateRequest.setNumber("existing_number");
        updateRequest.setUserId(1L);
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.existsByNumberAndUserId("existing_number", 1L)).thenReturn(true);
        assertThrows(CardNumberExistsException.class, () -> cardInfoService.updateCard(1L, updateRequest));
    }

    @Test
    void updateCardChangeUser() {
        CardRequest updateRequest = new CardRequest();
        updateRequest.setUserId(2L);
        updateRequest.setNumber(cardInfo.getNumber());

        User newUser = new User();
        newUser.setId(2L);

        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        doNothing().when(cardMapper).updateEntityFromRequest(updateRequest, cardInfo);
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardMapper.toResponse(cardInfo)).thenReturn(cardResponse);

        cardInfoService.updateCard(1L, updateRequest);
        verify(cache).evict(1L);
    }

    @Test
    void deleteCard() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        doNothing().when(cardInfoRepository).deleteById(1L);
        cardInfoService.deleteCard(1L);
        verify(cardInfoRepository).deleteById(1L);
        verify(cache).evict(1L);
    }

    @Test
    void deleteCard_NotFound() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardInfoService.deleteCard(1L));
    }
}