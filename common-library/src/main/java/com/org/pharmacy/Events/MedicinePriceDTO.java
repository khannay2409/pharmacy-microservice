package com.org.pharmacy.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicinePriceDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;

}