package service;

import org.example.dto.TransactionRequest;
import org.example.enums.TransactionType;
import org.example.exception.IncorrectAccountNumberException;
import org.example.model.Account;
import org.example.model.Transactions;
import org.example.repository.TransactionRepository;
import org.example.service.AccountService;
import org.example.service.TransactionService;
import org.example.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.example.service.impl.AccountServiceImpl.generateAccountNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private static String accountNumber;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    private Account account;
    private TransactionService transactionService;
    private TransactionRequest request;

    @BeforeEach
    public void setup() {
        initMocks(this);
        transactionService = new TransactionServiceImpl(transactionRepository, accountService);

        accountNumber = generateAccountNumber();

        account = Account.builder()
                .accountNumber(accountNumber)
                .name("Dj D")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.DEPOSIT)
                .pinCode("1234")
                .accountFromNumber(accountNumber)
                .accountToNumber(null)
                .build();
    }

    @Test
    void processTransferTransaction() {
        //GIVEN
        String recipientAccountNumber = generateAccountNumber();

        Account recipientAccount = Account.builder()
                .accountNumber(recipientAccountNumber)
                .name("Lui Rex")
                .balance(new BigDecimal(500))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);
        when(accountService.getByAccountNumber(recipientAccountNumber)).thenReturn(recipientAccount);

        request.setType(TransactionType.TRANSFER);
        request.setAccountToNumber(recipientAccountNumber);

        //WHEN
        transactionService.processTransaction(request);

        ArgumentCaptor<Transactions> transactionCaptor = ArgumentCaptor.forClass(Transactions.class);

        verify(transactionRepository).save(transactionCaptor.capture());

        Transactions savedTransaction = transactionCaptor.getValue();

        //THEN
        assertEquals(new BigDecimal(200), savedTransaction.getAmount());
        assertEquals(TransactionType.TRANSFER, savedTransaction.getTransactionType());
        assertEquals(accountNumber, savedTransaction.getAccountFromNumber());
        assertEquals(recipientAccountNumber, savedTransaction.getAccountToNumber());

        verify(transactionRepository).save(any(Transactions.class));
        verify(accountService, times(2)).update(any(Account.class));

        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(accountNumber).getBalance());
        assertEquals(new BigDecimal(700), accountService.getByAccountNumber(recipientAccountNumber).getBalance());
    }

    @Test
    void processDepositTransaction() {
        //GIVEN
        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        //WHEN
        transactionService.processTransaction(request);

        //THEN
        verify(transactionRepository).save(any(Transactions.class));
        verify(accountService).update(any(Account.class));
        assertEquals(new BigDecimal(1200), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void processWithdrawalTransaction() {
        //GIVEN
        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        //WHEN
        request.setType(TransactionType.WITHDRAWAL);
        transactionService.processTransaction(request);

        //THEN
        verify(transactionRepository).save(any(Transactions.class));
        verify(accountService).update(any(Account.class));
        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void processPaymentTransaction() {
        //GIVEN
        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        //WHEN
        request.setType(TransactionType.PAYMENT);
        transactionService.processTransaction(request);

        //THEN
        verify(transactionRepository).save(any(Transactions.class));
        verify(accountService).update(any(Account.class));
        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void getTransactionsByIncorrectAccountNumber() {
        //GIVEN || WHEN
        String incorrectAccountNumber = "12345"; // Некорректный номер счета

        //THEN
        assertThrows(IncorrectAccountNumberException.class, () -> {
            transactionService.getTransactionsByAccountNumber(incorrectAccountNumber);
        });
    }

}
