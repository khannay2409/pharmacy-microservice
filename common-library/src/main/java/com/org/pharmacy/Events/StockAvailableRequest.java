package com.org.pharmacy.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailableRequest {

    private Map<String, Integer> medicineQuantityMap;
}
