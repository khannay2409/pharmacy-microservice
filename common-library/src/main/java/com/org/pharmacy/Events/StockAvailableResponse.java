package com.org.pharmacy.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailableResponse {

    boolean inStock;

    List<MedicinePriceDTO> medicinePriceResponses;
}
