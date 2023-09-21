package org.example.enums;

public enum TransactionType {
    TRANSFER("Перевод"),
    DEPOSIT("Внесение"),
    WITHDRAWAL("Снятие"),
    PAYMENT("Платеж");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
}
