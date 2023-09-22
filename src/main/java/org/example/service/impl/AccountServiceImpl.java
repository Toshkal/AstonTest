package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.AccountInfo;
import org.example.exception.AccountNotFoundException;
import org.example.exception.IncorrectAccountNumberException;
import org.example.exception.IncorrectPinCodeException;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.service.AccountService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public void update(Account account) throws AccountNotFoundException, IncorrectAccountNumberException {
        getByAccountNumber(account.getAccountNumber());
        accountRepository.saveAndFlush(account);
    }

    @Override
    public Account getByAccountNumber(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.length() != 10) {
            throw new IncorrectAccountNumberException("Длина номера лицевого счета должна быть равна 10 символам");
        }
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        return account.orElseThrow(() -> new AccountNotFoundException(String.format("Лицевой счет %1$s не найден", accountNumber)));
    }

    @Override
    public Account add(AccountInfo accountInfo) throws IncorrectPinCodeException {
        String accountPinCode = accountInfo.getPinCode();
        if (accountPinCode == null || accountPinCode.isBlank() || accountPinCode.length() != 4) {
            throw new IncorrectPinCodeException(String.format("Некорректный Пин-код: %1$s", accountPinCode));
        }
        Account account = Account.builder().accountNumber(generateAccountNumber()).name(accountInfo.getName())
                .balance(accountInfo.getBalance()).pinCode(BCrypt.hashpw(accountPinCode, BCrypt.gensalt())).build();

        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAccountWithMaxBalance() throws AccountNotFoundException {
        List<Account> accountsWithMaxBalance = accountRepository.findWithMaxBalance();
        if (accountsWithMaxBalance.isEmpty()) {
            throw new AccountNotFoundException("Лицевого счета не существует, либо они пустые");
        }

        return accountsWithMaxBalance;
    }

    public static String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int digit = random.nextInt(10);
            accountNumber.append(digit);
        }

        return accountNumber.toString();
    }
}
