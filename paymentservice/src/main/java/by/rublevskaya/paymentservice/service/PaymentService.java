package by.rublevskaya.paymentservice.service;

import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.mapper.PaymentMapper;
import by.rublevskaya.paymentservice.model.Payment;
import by.rublevskaya.paymentservice.model.PaymentStatus;
import by.rublevskaya.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    private final RestTemplate restTemplate;

    @Value("${payment.external.random-api-url}")
    private String randomApiUrl;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setTimestamp(LocalDateTime.now());

        Integer randomNumber = getRandomNumberFromApi();

        if (randomNumber != null && randomNumber % 2 == 0) {
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    private Integer getRandomNumberFromApi() {
        try {
            String result = restTemplate.getForObject(randomApiUrl, String.class);
            return result != null ? Integer.parseInt(result.trim()) : null;
        } catch (Exception e) {
            log.error("External API call failed, defaulting to FAILED status", e);
            return 1;
        }
    }

    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findAllByOrderId(orderId);
        return paymentMapper.toResponseList(payments);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findAllByUserId(userId);
        return paymentMapper.toResponseList(payments);
    }

    public List<PaymentResponse> getPaymentsByStatuses(List<PaymentStatus> statuses) {
        List<Payment> payments = paymentRepository.findAllByStatusIn(statuses);
        return paymentMapper.toResponseList(payments);
    }

    public BigDecimal getTotalSum(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.sumAmountByDateRange(from, to);
    }
}