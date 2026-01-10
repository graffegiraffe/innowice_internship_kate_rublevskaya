package by.rublevskaya.paymentservice.integration;

import by.rublevskaya.paymentservice.AbstractIntegrationTest;
import by.rublevskaya.paymentservice.event.OrderEvent;
import by.rublevskaya.paymentservice.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class PaymentKafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldConsumeOrderEventAndProducePaymentEvent() throws InterruptedException {
        stubFor(get(urlEqualTo("/random")).willReturn(aResponse().withBody("2")));

        // Test Producer
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        KafkaTemplate<String, Object> testProducer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        // Test Consumer
        BlockingQueue<ConsumerRecord<String, OrderEvent>> records = new LinkedBlockingQueue<>();
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        var cf = new DefaultKafkaConsumerFactory<String, OrderEvent>(consumerProps);
        var containerProps = new ContainerProperties("create-payment-topic");
        var container = new KafkaMessageListenerContainer<>(cf, containerProps);
        container.setupMessageListener((MessageListener<String, OrderEvent>) records::add);
        container.start();

        Thread.sleep(1000);

        OrderEvent orderEvent = new OrderEvent(555L, 666L, new BigDecimal("200.00"), "PENDING");
        testProducer.send("create-order-topic", orderEvent);

        ConsumerRecord<String, OrderEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.value().getOrderId()).isEqualTo(555L);
        assertThat(received.value().getStatus()).isEqualTo("COMPLETED");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(paymentRepository.findAllByOrderId(555L)).isNotEmpty();
        });
        container.stop();
    }
}