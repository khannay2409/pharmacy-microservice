package com.org.pharmacy.Service;

import com.org.pharmacy.Entity.Medicine;
import com.org.pharmacy.Events.MedicinePriceDTO;
import com.org.pharmacy.Events.OrderItemDTO;
import com.org.pharmacy.Events.StockAvailableRequest;
import com.org.pharmacy.Events.StockAvailableResponse;
import com.org.pharmacy.Repository.MedicineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InventoryService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;


    public void updateStock(Long orderId, List<MedicinePriceDTO> items) {
        for (MedicinePriceDTO item : items) {
            Medicine medicine = medicineRepository.getReferenceById(item.getId());

            int updatedStock = medicine.getStockQuantity() - item.getQuantity();
            medicine.setStockQuantity(updatedStock);
            medicineRepository.save(medicine);

            if (updatedStock <= medicine.getLowStockThreshold()) {
                log.warn("⚠️ Low stock alert for medicine: {}", medicine.getName());
                // Optional: publish LowStockEvent
            }
        }
    }

    public StockAvailableResponse isStockAvailable(StockAvailableRequest request) {

        return checkStock(request.getMedicineQuantityMap());
    }

    private StockAvailableResponse checkStock(Map<String, Integer> medicineQuantityMap){
        List<MedicinePriceDTO> details = new ArrayList<>();

        boolean allInStock = medicineQuantityMap.entrySet().stream()
                .allMatch(entry -> {
                    Medicine medicine = medicineRepository.getMedicineDetails(entry.getKey());
                    if (medicine == null) {
                        return false;
                    }
                    boolean available = medicine.getStockQuantity() >= entry.getValue();
                    if (available) {
                        details.add(new MedicinePriceDTO(
                                medicine.getId(),
                                medicine.getName(),
                                medicine.getPrice(),
                                entry.getValue()
                        ));
                    }
                    return available;
                });

        return new StockAvailableResponse(allInStock, details);
    }

    private Medicine findMedicine(String medicineName) {
        return medicineRepository.getMedicineDetails(medicineName);
    }
}
