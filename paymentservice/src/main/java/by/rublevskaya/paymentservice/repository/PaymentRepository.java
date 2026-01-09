package by.rublevskaya.paymentservice.repository;

import by.rublevskaya.paymentservice.model.Payment;
import by.rublevskaya.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByOrderId(UUID orderId);

    List<Payment> findAllByUserId(UUID userId);

    List<Payment> findAllByStatusIn(List<PaymentStatus> statuses);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p " +
            "WHERE p.timestamp BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}