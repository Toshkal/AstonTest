package org.example.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.enums.TransactionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "Transactions")
public class Transactions {
    @Id
    @Column(name = "TransactionID")
    private UUID id;

    @Column(name = "Account_From_Number")
    private String accountFromNumber;

    @Column(name = "Account_To_Number")
    private String accountToNumber;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Column(name = "Transaction_Type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "Transaction_Date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
}
