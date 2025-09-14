package com.org.pharmacy.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItemDTO {
    private String medicineName;
    private Integer quantity;

}
