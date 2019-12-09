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

    private final AccountStorageService accountStorageService;

    public AccountServiceImpl(AccountStorageService accountStorageService) {
        this.accountStorageService = accountStorageService;
    }

    @Override
    public Account createAccount() {
        Account account = new Account();
        accountStorageService.addAccount(account);
        return account;
    }

    public boolean addCacheDeposit(Account account, MonetaryAmount amount) {
        try {
            return account.increaseAmount(amount);
        } catch (Exception e) {
            log.error("cant add cache deposit", e);
            return false;
        }
    }

    @Override
    public boolean transferMoney(Account from, Account to, MonetaryAmount amount) {
        try {
            MoneyUtils.checkAmountParameter(amount, to.getCurrency());
            MoneyUtils.checkAmountParameter(amount, from.getCurrency());
            Preconditions.checkArgument(from.getAmount().isGreaterThanOrEqualTo(amount),
                    "amount of money on sender account should be bigger or equal to transfer amount %s", amount);

            if (!from.decreaseAmount(amount)) {
                return false;
            }
            if (!to.increaseAmount(amount)) {
                from.decreaseAmount(amount);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error during transfer money", e);
            return false;
        }
    }
}
