package com.org.pharmacy.Repository;

import com.org.pharmacy.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Order getById(Long id);

}
