package org.fadeevm.moneytransfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fadeevm.moneytransfer.controller.v1.AccountController;
import org.fadeevm.moneytransfer.exceptions.MoneyTransferExceptionHandler;
import org.fadeevm.moneytransfer.exceptions.NotFoundException;
import org.fadeevm.moneytransfer.services.AccountService;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.fadeevm.moneytransfer.services.impl.AccountServiceImpl;
import org.fadeevm.moneytransfer.services.impl.AccountStorageServiceImpl;
import org.fadeevm.moneytransfer.utils.HealthIndicator;
import org.fadeevm.moneytransfer.utils.Spark;

import static spark.Spark.*;

@Slf4j
@Getter
public class MoneyTransferService {

    private final HealthIndicator healthIndicator;
    private final AccountStorageService storageService;
    private final AccountService accountService;
    private final ObjectMapper mapper;
    private final AccountController accountController;
    private final MoneyTransferExceptionHandler exceptionHandler;

    public MoneyTransferService() {
        this.healthIndicator = (request, response) -> "OK";
        this.storageService = new AccountStorageServiceImpl();
        this.accountService = new AccountServiceImpl(storageService);
        this.mapper = new ObjectMapper();
        accountController = new AccountController(storageService, accountService, mapper);
        exceptionHandler = new MoneyTransferExceptionHandler();
    }

    public static void main(String[] args) {
        MoneyTransferService moneyTransferService = new MoneyTransferService();
        moneyTransferService.start();
    }

    void start() {

        Spark.createServerWithRequestLog();


        get("/v1/health", healthIndicator::healthCheck);
        get("/v1/account/:id", accountController::getAccount);
        post("/v1/account", accountController::createAccount);
        patch("/v1/account/:id/deposit/:amount/:currency", accountController::addDeposit);
        patch("/v1/transfer/:from/:to/:amount", accountController::transfer);
        exception(NotFoundException.class, exceptionHandler::notFoundExceptionHandler);
        exception(IllegalArgumentException.class, exceptionHandler::illegalArgumentExceptionHandler);
    }

}
