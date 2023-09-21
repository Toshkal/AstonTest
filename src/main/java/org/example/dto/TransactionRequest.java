package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.enums.TransactionType;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TransactionRequest {
    private BigDecimal amount;
    private TransactionType type;
    private String pinCode;
    private String accountFromNumber;
    private String accountToNumber;
}
