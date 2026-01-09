package by.rublevskaya.paymentservice.mapper;

import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "paymentAmount", source = "amount")
    Payment toEntity(PaymentRequest request);

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}