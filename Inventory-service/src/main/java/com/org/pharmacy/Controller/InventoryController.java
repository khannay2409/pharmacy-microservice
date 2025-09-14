package com.org.pharmacy.Controller;

import com.org.pharmacy.Events.StockAvailableRequest;
import com.org.pharmacy.Events.StockAvailableResponse;
import com.org.pharmacy.Service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/checkStock")
    public StockAvailableResponse checkStockAvailable(@RequestBody StockAvailableRequest request)
    {
        log.info("received Stock availability request :{}",request);
        return inventoryService.isStockAvailable(request);
    }
}
