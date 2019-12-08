package org.fadeevm.moneytransfer;

import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountStorageService;

public class AccountStorageServiceMock implements AccountStorageService {
    @Override
    public Account getAccount(String id) {
        return Account.UNKNOWN_ACCOUNT;
    }

    @Override
    public Account addAccount(Account account) {
        return Account.UNKNOWN_ACCOUNT;
    }
}
