package by.rublevskaya.orderservice.integration;

import by.rublevskaya.orderservice.config.TestSecurityConfig;
import by.rublevskaya.orderservice.dto.OrderItemRequestDto;
import by.rublevskaya.orderservice.dto.OrderRequestDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.model.Item;
import by.rublevskaya.orderservice.repository.ItemRepository;
import by.rublevskaya.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@Testcontainers
@Import(TestSecurityConfig.class)
class OrderServiceIntegrationTest {

    private static final String TEST_AUTH_HEADER = "Bearer test-token";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2")
            .withDatabaseName("orders_db_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-schema.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.default-schema", () -> "orderservice");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "orderservice");
        registry.add("user-service.base-url", () -> "http://localhost:${wiremock.server.port}");
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        Item item = new Item();
        item.setName("Test Mac");
        item.setPrice(new BigDecimal("1000.00"));
        itemRepository.save(item);
    }

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @Test
    void createOrder() throws Exception {
        Long itemId = itemRepository.findAll().get(0).getId();

        OrderRequestDto request = new OrderRequestDto();
        request.setUserId(5L);
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setItemId(itemId);
        itemDto.setQuantity(1);
        request.setItems(List.of(itemDto));

        stubFor(get(urlEqualTo("/users/5"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": 5,
                                    "name": "Kate",
                                    "email": "kate@test.com"
                                }
                                """)));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                        .header("Authorization", TEST_AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name", is("Kate")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalPrice", is(1000.00)));
    }

    @Test
    void getOrderById() throws Exception {
        Long itemId = itemRepository.findAll().get(0).getId();
        stubFor(get(urlMatching("/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        OrderRequestDto request = new OrderRequestDto();
        request.setUserId(5L);
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setItemId(itemId);
        itemDto.setQuantity(1);
        request.setItems(List.of(itemDto));

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                        .header("Authorization", TEST_AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        OrderResponseDto createdOrder = objectMapper.readValue(responseJson, OrderResponseDto.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/orders/" + createdOrder.getId())
                        .header("Authorization", TEST_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name", containsString("Service Unavailable")));
    }

    @Test
    void deleteOrder() throws Exception {
        Long itemId = itemRepository.findAll().get(0).getId();
        OrderRequestDto request = new OrderRequestDto();
        request.setUserId(5L);
        request.setItems(List.of(new OrderItemRequestDto()));
        request.getItems().get(0).setItemId(itemId);
        request.getItems().get(0).setQuantity(1);

        stubFor(get(urlMatching("/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                        .header("Authorization", TEST_AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readValue(response, OrderResponseDto.class).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/orders/" + orderId)
                        .header("Authorization", TEST_AUTH_HEADER))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/orders/" + orderId)
                        .header("Authorization", TEST_AUTH_HEADER))
                .andExpect(status().isNotFound());
    }
}