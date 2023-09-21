package org.example.service;

import org.example.dto.AccountInfo;
import org.example.exception.AccountNotFoundException;
import org.example.exception.IncorrectPinCodeException;
import org.example.model.Account;

import java.util.List;

public interface AccountService {

    List<Account> getAll();

    void update(Account account);

    Account getByAccountNumber(String accountNumber) throws AccountNotFoundException;

    Account add(AccountInfo accountInfo) throws IncorrectPinCodeException;

    List<Account> getAccountWithMaxBalance() throws AccountNotFoundException;
}
