package org.fadeevm.moneytransfer.services;

import org.fadeevm.moneytransfer.models.Account;

import javax.money.MonetaryAmount;

public interface AccountService {
    Account createAccount();

    boolean addCacheDeposit(Account account, MonetaryAmount amount);

    boolean transferMoney(Account from, Account to, MonetaryAmount amount);
}
