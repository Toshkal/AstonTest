package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.TransactionRequest;
import org.example.exception.IncorrectAccountNumberException;
import org.example.exception.IncorrectPinCodeException;
import org.example.model.Account;
import org.example.enums.TransactionType;
import org.example.model.Transactions;
import org.example.repository.TransactionRepository;
import org.example.service.AccountService;
import org.example.service.TransactionService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Override
    @Transactional
    public void processTransaction(TransactionRequest request) throws IncorrectPinCodeException {
        Account accountFrom = accountService.getByAccountNumber(request.getAccountFromNumber());
        BigDecimal amount = request.getAmount();
        if (!request.getType().equals(TransactionType.DEPOSIT)) {
            String pinCode = request.getPinCode();

            if (pinCode == null || pinCode.isBlank()) {
                throw new IncorrectPinCodeException("ПинКод должен быть строго 4 значным");
            }
            if (!BCrypt.checkpw(pinCode, accountFrom.getPinCode())) {
                throw new IncorrectPinCodeException(String.format("Неверный Пин-код: %1;s", pinCode));
            }
            amount = amount.negate();
        }
        accountFrom.editBalance(amount);

        Transactions transactions = new Transactions();
        transactions.setId(UUID.randomUUID());
        transactions.setAmount(amount.abs());
        transactions.setTransactionType(request.getType());
        transactions.setAccountFromNumber(accountFrom.getAccountNumber());
        if (request.getType().equals(TransactionType.TRANSFER)) {
            Account accountTo = accountService.getByAccountNumber(request.getAccountToNumber());
            transactions.setAccountToNumber(accountTo.getAccountNumber());
            accountTo.editBalance(amount.abs());
            accountService.update(accountTo);
        }
        accountService.update(accountFrom);
        transactionRepository.save(transactions);
    }

    @Override
    public List<Transactions> getTransactionsByAccountNumber(String accountNumber) {
        if (accountNumber.length() != 10) {
            throw new IncorrectAccountNumberException("длина номера не равна 10 символам");
        }
        return transactionRepository.findAllByAccountFromNumber(accountNumber);
    }
}
