package org.example.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.exception.InsufficientFundsException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Table(name = "Accounts")
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "Account_Number")
    private final String accountNumber;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Pin_Code", nullable = false)
    private String pinCode;

    @Column(name = "Balance")
    private BigDecimal balance;

    public void editBalance(BigDecimal amount) throws InsufficientFundsException {
        if (balance.add(amount).compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException("Отрицательный баланс недопустим");
        }
        balance = balance.add(amount);
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setName(String name) {
        this.name = name;
    }
}
