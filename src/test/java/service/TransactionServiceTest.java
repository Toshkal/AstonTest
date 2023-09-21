package service;

import org.example.dto.TransactionRequest;
import org.example.exception.IncorrectAccountNumberException;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.enums.TransactionType;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;

    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        initMocks(this);
        transactionService = new TransactionServiceImpl(transactionRepository, accountService);
    }

    @Test
    void processTransferTransaction() {

        String senderAccountNumber = generateAccountNumber();
        String recipientAccountNumber = generateAccountNumber();

        Account senderAccount = Account.builder()
                .accountNumber(senderAccountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        Account recipientAccount = Account.builder()
                .accountNumber(recipientAccountNumber)
                .name("Jane Doe")
                .balance(new BigDecimal(500))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(senderAccountNumber)).thenReturn(senderAccount);
        when(accountService.getByAccountNumber(recipientAccountNumber)).thenReturn(recipientAccount);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.TRANSFER)
                .pinCode("1234")
                .accountFromNumber(senderAccountNumber)
                .accountToNumber(recipientAccountNumber)
                .build();

        transactionService.processTransaction(request);

        ArgumentCaptor<Transactions> transactionCaptor = ArgumentCaptor.forClass(Transactions.class);

        verify(transactionRepository).save(transactionCaptor.capture());

        Transactions savedTransaction = transactionCaptor.getValue();

        assertEquals(new BigDecimal(200), savedTransaction.getAmount());
        assertEquals(TransactionType.TRANSFER, savedTransaction.getTransactionType());
        assertEquals(senderAccountNumber, savedTransaction.getAccountFromNumber());
        assertEquals(recipientAccountNumber, savedTransaction.getAccountToNumber());


        verify(transactionRepository).save(any(Transactions.class));
        verify(accountService, times(2)).update(any(Account.class));

        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(senderAccountNumber).getBalance());
        assertEquals(new BigDecimal(700), accountService.getByAccountNumber(recipientAccountNumber).getBalance());
    }

    @Test
    void processDepositTransaction() {

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.DEPOSIT)
                .pinCode("1234")
                .accountFromNumber(accountNumber)
                .accountToNumber(null)
                .build();

        transactionService.processTransaction(request);

        verify(transactionRepository, times(1)).save(any(Transactions.class));
        verify(accountService, times(1)).update(any(Account.class));

        assertEquals(new BigDecimal(1200), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void processWithdrawalTransaction() {

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.WITHDRAWAL)
                .pinCode("1234")
                .accountFromNumber(accountNumber)
                .accountToNumber(null)
                .build();

        transactionService.processTransaction(request);

        verify(transactionRepository, times(1)).save(any(Transactions.class));
        verify(accountService, times(1)).update(any(Account.class));

        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void processPaymentTransaction() {

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.PAYMENT)
                .pinCode("1234")
                .accountFromNumber(accountNumber)
                .accountToNumber(null)
                .build();

        transactionService.processTransaction(request);

        verify(transactionRepository, times(1)).save(any(Transactions.class));
        verify(accountService, times(1)).update(any(Account.class));

        assertEquals(new BigDecimal(800), accountService.getByAccountNumber(accountNumber).getBalance());
    }

    @Test
    void processTransactionWithInsufficientFunds() {
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(100))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal(200))
                .type(TransactionType.WITHDRAWAL)
                .pinCode("1234")
                .accountFromNumber(accountNumber)
                .accountToNumber(null)
                .build();

        assertThrows(InsufficientFundsException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void getTransactionsByIncorrectAccountNumber() {
        String incorrectAccountNumber = "12345"; // Некорректный номер счета

        assertThrows(IncorrectAccountNumberException.class, () -> {
            transactionService.getTransactionsByAccountNumber(incorrectAccountNumber);
        });
    }

}
