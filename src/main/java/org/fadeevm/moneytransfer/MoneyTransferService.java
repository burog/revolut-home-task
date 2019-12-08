package org.fadeevm.moneytransfer;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.dto.v1.AccountDto;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.impl.AccountServiceImpl;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.fadeevm.moneytransfer.services.impl.AccountStorageServiceImpl;
import org.fadeevm.moneytransfer.utils.Spark;
import org.javamoney.moneta.Money;

import static spark.Spark.*;

@Slf4j
public class MoneyTransferService {

    public static void main(String[] args) {
        HealthIndicator healthIndicator = (request, response) -> "OK";
        AccountStorageService storageService = new AccountStorageServiceImpl();
        AccountService accountService = new AccountServiceImpl(storageService);

        Spark.createServerWithRequestLog();


        get("/v1/health", healthIndicator::healthCheck);
        get("/v1/account/:id", (request, response) -> {
            String id = request.params().getOrDefault(":id", Account.UNKNOWN_ACCOUNT_ID);
            Account account = storageService.getAccount(id);

            return new AccountDto(account).toString();
        });
        post("/v1/account", (request, response) -> {
            Account account = accountService.createAccount();
            return new AccountDto(account).toString();
        });
        patch("/v1/account/:id/deposit/:amount/:currency", (request, response) -> {
            String id = request.params().getOrDefault(":id", Account.UNKNOWN_ACCOUNT_ID);
            String amountStr = request.params().getOrDefault(":amount", "0");
            String currency = request.params().getOrDefault(":currency", "USD");
            float amount = Float.parseFloat(amountStr);

            Account account = storageService.getAccount(id);
            Preconditions.checkArgument(Account.UNKNOWN_ACCOUNT.equals(account), "please specify existing account");
            Money money = Money.of(amount, currency);
            return accountService.addCacheDeposit(account, money);
        });

    }
}
