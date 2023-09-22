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

import static org.example.service.impl.AccountServiceImpl.generateAccountNumber;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static String accountNumber;

    @Mock
    private AccountRepository accountRepository;

    private Account account;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        initMocks(this);
        accountService = new AccountServiceImpl(accountRepository);

        accountNumber = generateAccountNumber();

        account = Account.builder()
                .accountNumber(accountNumber)
                .name("Dj D")
                .balance(new BigDecimal(1000))
                .pinCode(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .build();
    }

    @Test
    void update() {
        //GIVEN

        accountRepository.save(account);

        when(accountRepository.findByAccountNumber(anyString())).thenReturn(java.util.Optional.of(account));

        account.setName("Lui Rex");

        //WHEN
        accountService.update(account);

        Account updatedAccount = accountService.getByAccountNumber(account.getAccountNumber());

        //THEN
        verify(accountRepository).saveAndFlush(account);
        assertEquals("Lui Rex", updatedAccount.getName());
    }

    @Test
    void updateWithNonExistentAccount() {
        try {
            accountService.update(account);
            fail("Должно быть выброшено исключение AccountNotFoundException");
        } catch (AccountNotFoundException e) {
            assertTrue(true);
        } catch (IncorrectAccountNumberException e) {
            fail("IncorrectAccountNumberException не должно быть выброшено");
        }
    }

    @Test
    void getByAccountNumber() {
        //GIVEN

        when(accountRepository.findByAccountNumber(account.getAccountNumber()))
                .thenReturn(Optional.of(account));

        //WHEN
        accountRepository.save(account);
        Account retrievedAccount = accountService.getByAccountNumber(accountNumber);

        //THEN
        assertEquals(account, retrievedAccount);
    }

    @Test
    void add() {
        //GIVEN
        AccountInfo accountInfo = AccountInfo.builder()
                .name("Dj D")
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

        //WHEN
        Account retrievedAccount = accountService.getByAccountNumber(account.getAccountNumber());

        //THEN
        assertNotNull(retrievedAccount);
        assertEquals(account.getAccountNumber(), retrievedAccount.getAccountNumber());
    }

    @Test
    void add_WithIncorrectPinCode() {
        // GIVEN || WHEN
        AccountInfo accountInfo = AccountInfo.builder()
                .name("Dj D")
                .balance(new BigDecimal(1000))
                .pinCode("12345")
                .build();

        //THEN
        assertThrows(IncorrectPinCodeException.class, () -> accountService.add(accountInfo));
    }
}