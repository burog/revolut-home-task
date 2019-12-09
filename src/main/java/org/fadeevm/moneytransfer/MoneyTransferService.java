package org.fadeevm.moneytransfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.dto.v1.AccountDto;
import org.fadeevm.moneytransfer.exceptions.NotFoundException;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.fadeevm.moneytransfer.services.impl.AccountServiceImpl;
import org.fadeevm.moneytransfer.services.impl.AccountStorageServiceImpl;
import org.fadeevm.moneytransfer.utils.HealthIndicator;
import org.fadeevm.moneytransfer.utils.Spark;
import org.javamoney.moneta.Money;

import static spark.Spark.*;

@Slf4j
@Getter
public class MoneyTransferService {

    private final HealthIndicator healthIndicator;
    private final AccountStorageService storageService;
    private final AccountService accountService;
    private final ObjectMapper mapper;

    public MoneyTransferService() {
        this.healthIndicator = (request, response) -> "OK";
        this.storageService = new AccountStorageServiceImpl();
        this.accountService = new AccountServiceImpl(storageService);
        this.mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        MoneyTransferService moneyTransferService = new MoneyTransferService();
        moneyTransferService.start();
    }

    void start() {

        Spark.createServerWithRequestLog();


        get("/v1/health", healthIndicator::healthCheck);
        get("/v1/account/:id", (request, response) -> {
            String id = request.params().get(":id");
            Account account = storageService.getAccount(id);

            if (Account.UNKNOWN_ACCOUNT.equals(account)) {
                throw new NotFoundException(id);
            }

            response.type("application/json");
            AccountDto accountDto = new AccountDto(account);
            return mapper.writeValueAsString(accountDto);
        });
        post("/v1/account", (request, response) -> {
            Account account = accountService.createAccount();
            response.status(201);
            response.type("application/json");
            return mapper.writeValueAsString(new AccountDto(account));
        });
        patch("/v1/account/:id/deposit/:amount/:currency", (request, response) -> {
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
        });
        exception(NotFoundException.class, (exception, request, response) -> {
            log.info("handling error with status 404", exception);
            response.status(404);
            response.type("application/json");
            response.body("{\"Error\":\"Not found: " + exception.getMessage() + "\"}");
        });
        exception(IllegalArgumentException.class, (exception, request, response) -> {
            log.info("handling error with status 400", exception);
            response.status(400);
            response.type("application/json");
            response.body("{\"Error\":\"Illegal input: " + exception.getMessage() + "\"}");
        });
    }

}
