package org.fadeevm.moneytransfer.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fadeevm.moneytransfer.dto.v1.AccountDto;
import org.fadeevm.moneytransfer.exceptions.NotFoundException;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.javamoney.moneta.Money;
import spark.Request;
import spark.Response;

public class AccountController {
    private final AccountStorageService storageService;
    private final AccountService accountService;
    private final ObjectMapper mapper;


    public AccountController(AccountStorageService storageService,
                             AccountService accountService, ObjectMapper mapper) {
        this.storageService = storageService;
        this.accountService = accountService;
        this.mapper = mapper;
    }

    public Object transfer(Request request, Response response) {
        String accountIdFrom = request.params().get(":from");
        String accountIdTo = request.params().get(":to");

        String amountStr = request.params().getOrDefault(":amount", "0");
        float amount = Float.parseFloat(amountStr);

        Account accountFrom = storageService.getAccount(accountIdFrom);
        Account accountTo = storageService.getAccount(accountIdTo);

        if (Account.UNKNOWN_ACCOUNT.equals(accountTo)) {
            throw new NotFoundException(accountIdTo);
        } else if (Account.UNKNOWN_ACCOUNT.equals(accountFrom)) {
            throw new NotFoundException(accountIdFrom);
        }

        Money money = Money.of(amount, accountFrom.getCurrency());
        response.type("application/json");
        boolean isSuccessfulAddedDeposit = accountService.transferMoney(accountFrom, accountTo, money);
        if (!isSuccessfulAddedDeposit) {
            throw new IllegalStateException("Can't transfer money " + money + " from " + accountFrom.getId() + " to " + accountTo.getId());
        }
        return "";
    }

    public Object addDeposit(Request request, Response response) {
        String id = request.params().get(":id");

        String amountStr = request.params().getOrDefault(":amount", "0");
        String currency = request.params().getOrDefault(":currency", "USD");
        float amount = Float.parseFloat(amountStr);

        Account account = storageService.getAccount(id);
        if (Account.UNKNOWN_ACCOUNT.equals(account)) {
            throw new NotFoundException(id);
        }

        Money money = Money.of(amount, currency);
        response.type("application/json");
        boolean isSuccessfulAddedDeposit = accountService.addCacheDeposit(account, money);
        if (!isSuccessfulAddedDeposit) {
            throw new IllegalStateException("Can't add cache deposit for account #" + id);
        }
        return "";
    }

    public Object createAccount(Request request, Response response) throws JsonProcessingException {
        Account account = accountService.createAccount();
        response.status(201);
        response.type("application/json");
        return mapper.writeValueAsString(new AccountDto(account));
    }

    public Object getAccount(Request request, Response response) throws JsonProcessingException {
        String id = request.params().get(":id");
        Account account = storageService.getAccount(id);

        if (Account.UNKNOWN_ACCOUNT.equals(account)) {
            throw new NotFoundException(id);
        }

        response.type("application/json");
        AccountDto accountDto = new AccountDto(account);
        return mapper.writeValueAsString(accountDto);
    }
}
