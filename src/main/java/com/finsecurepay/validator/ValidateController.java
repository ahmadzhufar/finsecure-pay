package com.finsecurepay.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ValidateController {

    // This reads the DAILY_LIMIT from application.properties or Kubernetes ConfigMap
    @Value("${DAILY_LIMIT:10000.00}")
    private String dailyLimitString;

    @PostMapping("/validate")
    public Map<String, Object> validate(@RequestBody Map<String, Object> request) {
        List<String> reasons = new ArrayList<>();
        BigDecimal dailyLimit = new BigDecimal(dailyLimitString);

        String accountId = (String) request.get("accountId");
        String destinationAccountId = (String) request.get("destinationAccountId");
        String currency = (String) request.get("currency");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        if (accountId == null || accountId.isBlank()) {
            reasons.add("ACCOUNT_ID_REQUIRED");
        }
        if (destinationAccountId == null || destinationAccountId.isBlank()) {
            reasons.add("DESTINATION_ACCOUNT_ID_REQUIRED");
        }
        if (accountId != null && accountId.equals(destinationAccountId)) {
            reasons.add("SELF_TRANSFER_NOT_ALLOWED");
        }
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            reasons.add("AMOUNT_TOO_SMALL");
        }
        
        // Compare against the ConfigMap value!
        if (amount.compareTo(dailyLimit) > 0) {
            reasons.add("AMOUNT_EXCEEDS_DAILY_LIMIT");
        }
        
        if (currency == null || !List.of("USD", "EUR", "GBP", "SGD").contains(currency)) {
            reasons.add("UNSUPPORTED_CURRENCY");
        }

        if (reasons.isEmpty()) {
            return Map.of("valid", true, "reasons", List.of());
        } else {
            return Map.of("valid", false, "reasons", reasons);
        }
    }
}