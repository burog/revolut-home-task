package org.fadeevm.moneytransfer.services.impl;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.spi.MoneyUtils;

import javax.money.MonetaryAmount;

@Slf4j
public class AccountServiceImpl implements AccountService {

    public static final String DEFAULT_CURRENCY_CODE = "USD";
    public static final int DEFAULT_AMOUNT = 0;
    private final AccountStorageService accountStorageService;

    public AccountServiceImpl(AccountStorageService accountStorageService) {
        this.accountStorageService = accountStorageService;
    }

    @Override
    public Account createAccount() {
        Account account = new Account();
        account.setAmount(Money.of(DEFAULT_AMOUNT, DEFAULT_CURRENCY_CODE));
        accountStorageService.addAccount(account);
        return account;
    }

    public boolean addCacheDeposit(Account account, MonetaryAmount amount) {
        try {
            account.setAmount(account.getAmount().add(amount));
        } catch (Exception e) {
            log.error("cant add cache deposit", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean transferMoney(Account from, Account to, MonetaryAmount amount) {
        try {
            //TODO add lock or CAS
            MoneyUtils.checkAmountParameter(amount, to.getCurrency());
            MoneyUtils.checkAmountParameter(amount, from.getCurrency());
            Preconditions.checkArgument(from.getAmount().isGreaterThanOrEqualTo(amount),
                    "amount of money on sender account should be bigger or equal to transfer amount {}", amount);

            MonetaryAmount subtract = from.getAmount().subtract(amount);
            from.setAmount(subtract);

            MonetaryAmount add = to.getAmount().add(amount);
            to.setAmount(add);
            return true;
        } catch (Exception e) {
            log.error("Error during transfer money", e);
            return false;
        }
    }
}
