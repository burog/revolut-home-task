package org.fadeevm.moneytransfer;

import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.fadeevm.moneytransfer.services.impl.AccountServiceImpl;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AccountDtoServiceImplTest {

    private static final CurrencyUnit USD = Monetary.getCurrency("USD");
    private static final double TEN_WITH_51 = 10.51;
    private static final Money oneCent = Money.of(0.01, "USD");
    private static final MonetaryAmount TEN_DOLLARS_WITH_51_CENT = Money.of(TEN_WITH_51, "USD");
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
        assertEquals(account.getAmount().getNumber().doubleValueExact(), TEN_WITH_51);
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
    void moveFullAmountOfUsd() {
        accountService.addCacheDeposit(accountY, TEN_DOLLARS_WITH_51_CENT);

        boolean result = accountService.transferMoney(accountY, accountX, TEN_DOLLARS_WITH_51_CENT);
        assertTrue(result);
        assertEquals(Money.of(0, "USD"), accountY.getAmount());
        assertEquals(Money.of(AccountDtoServiceImplTest.TEN_WITH_51, "USD"), accountX.getAmount());
    }

    @Test
    void moveIllegalAmountOfUsd() {
        accountService.addCacheDeposit(accountY, Money.of(5, "USD"));

        boolean result = accountService.transferMoney(accountY, accountX, TEN_DOLLARS_WITH_51_CENT);
        assertEquals(Money.of(5, "USD"), accountY.getAmount());
        assertEquals(Money.of(0, "USD"), accountX.getAmount());
        assertFalse(result);
    }

    @Test
    void concurrentMoneyTransfer() throws InterruptedException, ExecutionException {
        Account accountSource = accountService.createAccount();
        Money TEN_DOLLARS = Money.of(10, "USD");
        accountService.addCacheDeposit(accountSource, TEN_DOLLARS);


        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Future<Integer> futureNumbersOfCentForAccountX = executorService.submit(
                () -> transferAllMoneyToAccount(accountSource, accountX));
        Future<Integer> futureNumbersOfCentForAccountY = executorService.submit(
                () -> transferAllMoneyToAccount(accountSource, accountY));
        Future<Integer> futureNumbersOfCentForAccountY2 = executorService.submit(
                () -> transferAllMoneyToAccount(accountSource, accountY));
        Future<Integer> futureNumbersOfCentForAccountY3 = executorService.submit(
                () -> transferAllMoneyToAccount(accountSource, accountY));

        Integer numbersOfCentForAccountX = futureNumbersOfCentForAccountX.get();
        Integer numbersOfCentForAccountY = futureNumbersOfCentForAccountY.get();
        Integer numbersOfCentForAccountY2 = futureNumbersOfCentForAccountY.get();
        Integer numbersOfCentForAccountY3 = futureNumbersOfCentForAccountY.get();

        log.info("amount source = {}, sum of tries {}", accountSource.getAmount(), numbersOfCentForAccountY + numbersOfCentForAccountX);
        log.info("numbersOfCentForAccountX = {}, amount accountX = {}", numbersOfCentForAccountX, accountX.getAmount());
        log.info("numbersOfCentForAccountY = {}, amount accountY = {}", numbersOfCentForAccountY, accountY.getAmount());

        assertEquals(TEN_DOLLARS, accountX.getAmount().add(accountY.getAmount()).add(accountSource.getAmount()));
    }

    private Integer transferAllMoneyToAccount(Account from, Account to) {
        boolean result;
        int i = 0;
        do {
            result = accountService.transferMoney(from, to, oneCent);
            i++;
        } while (result);
        return i;
    }
}