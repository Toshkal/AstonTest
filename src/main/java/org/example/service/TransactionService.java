package org.example.service;

import org.example.dto.TransactionRequest;
import org.example.model.Transactions;

import java.util.List;

public interface TransactionService {

    void processTransaction(TransactionRequest request);

    List<Transactions> getTransactionsByAccountNumber(String accountNumber);
}
