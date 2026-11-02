package com.finsecurepay.validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionValidatorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldReturnUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.service").value("finsecure-pay"));
    }

    @Test
    void validTransactionShouldReturnValidTrue() throws Exception {
        String jsonRequest = """
                {"accountId":"A","destinationAccountId":"B","amount":100,"currency":"USD"}
                """;
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.reasons").isEmpty());
    }

    @Test
    void selfTransferShouldBeRejected() throws Exception {
        String jsonRequest = """
                {"accountId":"A","destinationAccountId":"A","amount":100,"currency":"USD"}
                """;
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reasons[0]").value("SELF_TRANSFER_NOT_ALLOWED"));
    }

    @Test
    void amountExceedingLimitShouldBeRejected() throws Exception {
        String jsonRequest = """
                {"accountId":"A","destinationAccountId":"B","amount":15000,"currency":"USD"}
                """;
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reasons[0]").value("AMOUNT_EXCEEDS_DAILY_LIMIT"));
    }
}