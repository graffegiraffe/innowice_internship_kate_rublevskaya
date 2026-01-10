package by.rublevskaya.paymentservice.service;

import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.mapper.PaymentMapper;
import by.rublevskaya.paymentservice.model.Payment;
import by.rublevskaya.paymentservice.model.PaymentStatus;
import by.rublevskaya.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest request;
    private Payment paymentEntity;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "randomApiUrl", "http://test-api.com");

        request = new PaymentRequest();
        request.setOrderId(1L);
        request.setUserId(100L);
        request.setAmount(new BigDecimal("100.00"));

        paymentEntity = new Payment();
        paymentEntity.setId(1L);
        paymentEntity.setOrderId(1L);
        paymentEntity.setUserId(100L);
        paymentEntity.setPaymentAmount(new BigDecimal("100.00"));

        paymentResponse = new PaymentResponse();
        paymentResponse.setId(1L);
        paymentResponse.setOrderId(1L);
        paymentResponse.setStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void createPayment_Success() {
        when(paymentMapper.toEntity(request)).thenReturn(paymentEntity);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("2");
        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentEntity);
        when(paymentMapper.toResponse(paymentEntity)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository).save(paymentEntity);
    }

    @Test
    void createPayment_Failed() {
        when(paymentMapper.toEntity(request)).thenReturn(paymentEntity);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("1");
        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentEntity);

        PaymentResponse failedResponse = new PaymentResponse();
        failedResponse.setStatus(PaymentStatus.FAILED);
        when(paymentMapper.toResponse(paymentEntity)).thenReturn(failedResponse);

        PaymentResponse result = paymentService.createPayment(request);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentEntity.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Create Payment failed API Exception")
    void createPayment_Failed_Api() {
        when(paymentMapper.toEntity(request)).thenReturn(paymentEntity);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Down"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentEntity);

        PaymentResponse failedResponse = new PaymentResponse();
        failedResponse.setStatus(PaymentStatus.FAILED);
        when(paymentMapper.toResponse(paymentEntity)).thenReturn(failedResponse);

        PaymentResponse result = paymentService.createPayment(request);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void getPaymentsByOrderId() {
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(Collections.singletonList(paymentEntity));
        when(paymentMapper.toResponseList(any())).thenReturn(Collections.singletonList(paymentResponse));

        List<PaymentResponse> results = paymentService.getPaymentsByOrderId(1L);

        assertThat(results).hasSize(1);
        verify(paymentRepository).findAllByOrderId(1L);
    }

    @Test
    void getPaymentsByUserId() {
        when(paymentRepository.findAllByUserId(100L)).thenReturn(Collections.singletonList(paymentEntity));
        when(paymentMapper.toResponseList(any())).thenReturn(Collections.singletonList(paymentResponse));

        List<PaymentResponse> results = paymentService.getPaymentsByUserId(100L);

        assertThat(results).hasSize(1);
        verify(paymentRepository).findAllByUserId(100L);
    }

    @Test
    void getPaymentsByStatuses() {
        List<PaymentStatus> statuses = List.of(PaymentStatus.COMPLETED);
        when(paymentRepository.findAllByStatusIn(statuses)).thenReturn(Collections.singletonList(paymentEntity));
        when(paymentMapper.toResponseList(any())).thenReturn(Collections.singletonList(paymentResponse));

        List<PaymentResponse> results = paymentService.getPaymentsByStatuses(statuses);

        assertThat(results).hasSize(1);
        verify(paymentRepository).findAllByStatusIn(statuses);
    }

    @Test
    @DisplayName("Get Total Sum")
    void getTotalSum() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal expectedSum = new BigDecimal("500.00");
        when(paymentRepository.sumAmountByDateRange(any(), any())).thenReturn(expectedSum);

        BigDecimal result = paymentService.getTotalSum(now.minusDays(1), now);

        assertThat(result).isEqualTo(expectedSum);
        verify(paymentRepository).sumAmountByDateRange(any(), any());
    }
}