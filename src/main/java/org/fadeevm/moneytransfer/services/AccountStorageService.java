package org.fadeevm.moneytransfer.services;

import org.fadeevm.moneytransfer.models.Account;

public interface AccountStorageService {
    Account getAccount(String id);
    Account addAccount(Account account);
}
