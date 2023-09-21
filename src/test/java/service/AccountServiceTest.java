package service;

import org.example.dto.AccountInfo;
import org.example.exception.AccountNotFoundException;
import org.example.exception.IncorrectAccountNumberException;
import org.example.exception.IncorrectPinCodeException;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.service.AccountService;
import org.example.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.example.service.impl.AccountServiceImpl.generateAccountNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    public void setup() {
        initMocks(this);
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    void update() {
        Account accountToUpdate = Account.builder()
                .accountNumber(generateAccountNumber())
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .id(UUID.randomUUID())
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt())).build();

        accountRepository.save(accountToUpdate);

        when(accountRepository.findByAccountNumber(anyString())).thenReturn(java.util.Optional.of(accountToUpdate));

        accountToUpdate.setName("Jane Doe");

        accountService.update(accountToUpdate);

        verify(accountRepository, times(1)).saveAndFlush(accountToUpdate);

        Account updatedAccount = accountService.getByAccountNumber(accountToUpdate.getAccountNumber());
        assertEquals("Jane Doe", updatedAccount.getName());
    }

    @Test
    void updateWithInvalidAccountNumber() {
        Account accountToUpdate = Account.builder()
                .accountNumber("1234")
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .id(UUID.randomUUID())
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        try {
            accountService.update(accountToUpdate);
            fail("Должно быть выброшено исключение IncorrectAccountNumberException");
        } catch (IncorrectAccountNumberException e) {
            assertTrue(true);
        } catch (AccountNotFoundException e) {
            fail("AccountNotFoundException не должно быть выброшено");
        }
    }

    @Test
    void updateWithNonExistentAccount() {
        String nonExistentAccountNumber = generateAccountNumber();

        Account accountToUpdate = Account.builder()
                .accountNumber(nonExistentAccountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .id(UUID.randomUUID())
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        try {
            accountService.update(accountToUpdate);
            fail("Должно быть выброшено исключение AccountNotFoundException");
        } catch (AccountNotFoundException e) {
            assertTrue(true);
        } catch (IncorrectAccountNumberException e) {
            fail("IncorrectAccountNumberException не должно быть выброшено");
        }
    }

    @Test
    void getByAccountNumber() {
        String accountNumber = generateAccountNumber();

        Account accountToRetrieve = Account.builder()
                .accountNumber(accountNumber)
                .name("Jhon Doe")
                .balance(new BigDecimal(1000))
                .id(UUID.randomUUID())
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();

        accountRepository.save(accountToRetrieve);

        when(accountRepository.findByAccountNumber(accountToRetrieve.getAccountNumber()))
                .thenReturn(Optional.of(accountToRetrieve));
        Account retrievedAccount = accountService.getByAccountNumber(accountNumber);

        assertEquals(accountToRetrieve, retrievedAccount);
    }

    @Test
    void add() {
        AccountInfo accountInfo = AccountInfo.builder()
                .name("Jane Doe")
                .balance(new BigDecimal(1000))
                .pinCode("1234")
                .build();

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            return Account.builder().accountNumber(savedAccount.getAccountNumber())
                    .name(savedAccount.getName()).balance(savedAccount.getBalance())
                    .pinCode(savedAccount.getPinCode()).build();
        });

        Account account = accountService.add(accountInfo);

        assertNotNull(account);

        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));
        Account retrievedAccount = accountService.getByAccountNumber(account.getAccountNumber());
        assertNotNull(retrievedAccount);
        assertEquals(account.getAccountNumber(), retrievedAccount.getAccountNumber());
    }

    @Test
    void add_WithIncorrectPinCode() {
        AccountInfo accountInfo = AccountInfo.builder()
                .name("Jane Doe")
                .balance(new BigDecimal(1000))
                .pinCode("12345")
                .build();

        assertThrows(IncorrectPinCodeException.class, () -> accountService.add(accountInfo));
    }
}