package by.rublevskaya.orderservice.repository;

import by.rublevskaya.orderservice.model.Order;
import by.rublevskaya.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByIdIn(Collection<Long> ids);
    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);
}