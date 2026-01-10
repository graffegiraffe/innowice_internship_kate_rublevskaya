package by.rublevskaya.paymentservice.integration;

import by.rublevskaya.paymentservice.AbstractIntegrationTest;
import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.model.PaymentStatus;
import by.rublevskaya.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentAndSaveToDatabase() {
        // even means COMPLETED
        stubFor(get(urlEqualTo("/random"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("2")));

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(123L);
        request.setUserId(456L);
        request.setAmount(new BigDecimal("99.99"));

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        var paymentsInDb = paymentService.getPaymentsByOrderId(123L);
        assertThat(paymentsInDb).hasSize(1);
        assertThat(paymentsInDb.get(0).getPaymentAmount()).isEqualByComparingTo("99.99");
    }

    @Test
    void shouldHandleExternalApiFailureAndSaveFailedStatus() {
        // 500 error
        stubFor(get(urlEqualTo("/random"))
                .willReturn(aResponse().withStatus(500)));

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(777L);
        request.setUserId(888L);
        request.setAmount(new BigDecimal("50.00"));

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
