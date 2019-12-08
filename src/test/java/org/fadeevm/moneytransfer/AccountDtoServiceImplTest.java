package org.fadeevm.moneytransfer;

import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.impl.AccountServiceImpl;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import static org.junit.jupiter.api.Assertions.*;

class AccountDtoServiceImplTest {

    public static final CurrencyUnit USD = Monetary.getCurrency("USD");
    public static final double TEN_WITH_51_ = 10.51;
    private static final MonetaryAmount TEN_DOLLARS_WITH_51_CENT = Money.of(TEN_WITH_51_, "USD");
    private AccountService accountService;
    private Account accountX;
    private Account accountY;

    @BeforeEach
    void setUp() {
        AccountStorageService accountStorageService = new AccountStorageServiceMock();
        accountService = new AccountServiceImpl(accountStorageService);

        accountX = accountService.createAccount();
        accountY = accountService.createAccount();
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void testInitStateOfAccount() {
        Account account = accountService.createAccount();
        CurrencyUnit currency = account.getCurrency();
        MonetaryAmount amount = account.getAmount();
        assertEquals(currency.getCurrencyCode(), "USD");
        assertEquals(currency.getNumericCode(), 840);
        assertTrue(amount.isZero());
    }

    @Test
    void addCacheDeposit() {
        Account account = accountService.createAccount();
        boolean successful = accountService.addCacheDeposit(account, TEN_DOLLARS_WITH_51_CENT);
        assertTrue(successful);
        assertEquals(account.getAmount(), TEN_DOLLARS_WITH_51_CENT);
        assertEquals(account.getCurrency(), USD);
        assertEquals(account.getAmount().getNumber().doubleValueExact(), TEN_WITH_51_);
    }

    @Test
    void moveLegalAmountOfUsd() {
        accountService.addCacheDeposit(accountY, Money.of(11, "USD"));

        boolean result = accountService.transferMoney(accountY, accountX, TEN_DOLLARS_WITH_51_CENT);
        assertTrue(result);
        assertEquals(Money.of(0.49, "USD"), accountY.getAmount());
        assertEquals(Money.of(10.51, "USD"), accountX.getAmount());
    }

    @Test
    void moveIllegalAmountOfUsd() {
        accountService.addCacheDeposit(accountY, Money.of(5, "USD"));

        boolean result = accountService.transferMoney(accountY, accountX, TEN_DOLLARS_WITH_51_CENT);
        assertEquals(Money.of(5, "USD"), accountY.getAmount());
        assertEquals(Money.of(0, "USD"), accountX.getAmount());
        assertFalse(result);
    }
}