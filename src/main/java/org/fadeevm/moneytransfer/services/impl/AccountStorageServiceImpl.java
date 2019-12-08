package org.fadeevm.moneytransfer.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountStorageService;

import java.util.concurrent.ConcurrentHashMap;

import static org.fadeevm.moneytransfer.models.Account.UNKNOWN_ACCOUNT;

@Slf4j
public class AccountStorageServiceImpl implements AccountStorageService {
    private final ConcurrentHashMap<String, Account> store = new ConcurrentHashMap<>();



    @Override
    public Account getAccount(String id) {
        log.trace("try to find in storage account by id {}", id);
        return store.getOrDefault(id, UNKNOWN_ACCOUNT);
    }

    @Override
    public Account addAccount(Account account) {
        log.trace("try to add to storage account {}", account);
        return store.putIfAbsent(account.getId(), account);
    }


}
