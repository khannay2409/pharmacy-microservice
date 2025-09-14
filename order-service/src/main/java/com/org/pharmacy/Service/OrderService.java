package com.org.pharmacy.Service;

import com.org.pharmacy.Enum.OrderStatus;
import com.org.pharmacy.Enum.PaymentStatus;
import com.org.pharmacy.Events.*;
import com.org.pharmacy.Entity.Order;
import com.org.pharmacy.Entity.OrderItem;
import com.org.pharmacy.Gateways.InventoryClient;
import com.org.pharmacy.Repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private InventoryClient inventoryClient;

    @Transactional
    public Order placeOrder(Long userId, List<OrderItemDTO> itemsDto) {

        StockAvailableResponse response = checkStock(itemsDto);

        return createOrder(userId,itemsDto,response);
    }

    private Order createOrder(Long userId, List<OrderItemDTO> itemsDto, StockAvailableResponse response)
    {
        Order order = buildBaseOrder(userId, response.isInStock());

        List<OrderItem> orderItems = buildOrderItems(order, itemsDto, response);
        order.setItems(orderItems);

        BigDecimal totalAmount = calculateTotalAmount(itemsDto, response);
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);

        if (response.isInStock()) {
            publishOrderEvent(order, response);
        }

        return order;
    }

    private StockAvailableResponse checkStock(List<OrderItemDTO> itemsDto)
    {
        Map<String, Integer> items = itemsDto.stream()
                .collect(Collectors.toMap(OrderItemDTO::getMedicineName, OrderItemDTO::getQuantity));
        log.info("Medicine and Quantity Map :{}",items);
        StockAvailableRequest request = new StockAvailableRequest();
        request.setMedicineQuantityMap(items);
        return inventoryClient.checkStock(request);
    }

    @Transactional
    public void updateStatus(Long orderId, PaymentStatus status)
    {
        Order order = orderRepository.getById(orderId);
        order.setStatus(status == PaymentStatus.SUCCESS ? OrderStatus.COMPLETED : OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void publishOrderEvent(Order orderCreated, StockAvailableResponse response){
        OrderPlacedEvent event = new OrderPlacedEvent(
                orderCreated.getId(),
                orderCreated.getUserId(),
                orderCreated.getTotalAmount(),
                Instant.now(),
                response.getMedicinePriceResponses()
        );

        // Register to publish after successful commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send("orders.events", event);
            }
        });
    }

    private Order buildBaseOrder(Long userId, boolean inStock) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(inStock ? OrderStatus.PLACED : OrderStatus.CANCELLED);
        return order;
    }

    private List<OrderItem> buildOrderItems(Order order, List<OrderItemDTO> itemsDto, StockAvailableResponse response) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO dto : itemsDto) {
            MedicinePriceDTO medicinePrice = findMedicinePrice(response, dto.getMedicineName());

            OrderItem orderItem = new OrderItem();
            orderItem.setMedicineId(medicinePrice.getId());
            orderItem.setQuantity(dto.getQuantity());
            orderItem.setPrice(medicinePrice.getPrice().doubleValue());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private BigDecimal calculateTotalAmount(List<OrderItemDTO> itemsDto, StockAvailableResponse response) {
        return itemsDto.stream()
                .map(dto -> {
                    MedicinePriceDTO medicinePrice = findMedicinePrice(response, dto.getMedicineName());
                    return medicinePrice.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MedicinePriceDTO findMedicinePrice(StockAvailableResponse response, String medicineName) {
        return response.getMedicinePriceResponses().stream()
                .filter(m -> m.getName().equalsIgnoreCase(medicineName))
                .findFirst()
                .get(); // safe under assumption: inventory always returns
    }

}

