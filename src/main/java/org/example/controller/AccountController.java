package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.example.dto.AccountInfo;
import org.example.model.Account;
import org.example.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("account")
@Api(tags = "Bank accounts API")
public class AccountController {
    private final AccountService accountService;

    @PostMapping()
    @ApiOperation("Create new bank account")
    public ResponseEntity<Account> addNewAccount(@RequestBody AccountInfo accountInfo) {
        return ResponseEntity.ok(accountService.add(accountInfo));
    }

    @GetMapping()
    @ApiOperation("Get all bank accounts")
    public ResponseEntity<List<AccountInfo>> getAllAccounts() {
        List<Account> accounts = accountService.getAll();

        List<AccountInfo> accountInfos = getAccountInfos(accounts);

        return ResponseEntity.ok(accountInfos);
    }

    @GetMapping("/{accountNumber}")
    @ApiOperation("Get bank accont by account number")
    public ResponseEntity<Account> getAccountById(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getByAccountNumber(accountNumber));
    }

    @GetMapping("/maxBalance")
    @ApiOperation("Get bank account with max balance")
    public ResponseEntity<List<AccountInfo>> getAccountWithMaxBalance() {
        List<Account> accounts = accountService.getAccountWithMaxBalance();

        List<AccountInfo> accountInfos = getAccountInfos(accounts);

        return ResponseEntity.ok(accountInfos);
    }

    private List<AccountInfo> getAccountInfos(List<Account> accounts) {
        return accounts.stream()
                .map(account -> new AccountInfo(account.getName(), account.getBalance(), null))
                .collect(Collectors.toList());
    }
}
