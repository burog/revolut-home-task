package org.fadeevm.moneytransfer.exceptions;

import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

@Slf4j
public class MoneyTransferExceptionHandler {
    public void illegalArgumentExceptionHandler(IllegalArgumentException exception, Request request, Response response) {
        log.info("handling error with status 400", exception);
        response.status(400);
        response.type("application/json");
        response.body("{\"Error\":\"Illegal input: " + exception.getMessage() + "\"}");
    }

    public void notFoundExceptionHandler(NotFoundException exception, Request request, Response response) {
        log.info("handling error with status 404", exception);
        response.status(404);
        response.type("application/json");
        response.body("{\"Error\":\"Not found: " + exception.getMessage() + "\"}");
    }

}
