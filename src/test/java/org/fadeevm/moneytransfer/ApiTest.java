package org.fadeevm.moneytransfer;


import okhttp3.*;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountStorageService;
import org.javamoney.moneta.Money;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import spark.Spark;

import java.io.IOException;

public class ApiTest {

    private static final CustomComparator IGNORE_ID_COMPARATOR = new CustomComparator(JSONCompareMode.LENIENT,
            Customization.customization("id", (o1, o2) -> true));
    private static String BASE_URL;
    private static OkHttpClient client;
    private static AccountStorageService accountStorageService;

    @BeforeAll
    public static void startServer() {
        MoneyTransferService moneyTransferService = new MoneyTransferService();
        moneyTransferService.start();
        accountStorageService = moneyTransferService.getStorageService();
        int PORT = Spark.port();
        client = new OkHttpClient();
        BASE_URL = "http://localhost:" + PORT;

    }

    @AfterAll
    public static void stopServer() {
        Spark.stop();
    }

    @Test
    void checkHealth() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/health")
                .get()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertEquals(200, response.code());
        Assertions.assertNotNull(response.body());
        Assertions.assertEquals("OK", response.body().string());
    }

    @Test
    void checkCreatingNewAccount() throws IOException, JSONException {
        final String account = "{\"id\":\"SOME_ID\",\"currency\":\"USD\",\"amount\":0.0}";

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/account")
                .post(RequestBody.create(null, new byte[]{}))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertAll("assert http params",
                () -> Assertions.assertEquals(201, response.code()),
                () -> Assertions.assertEquals("application/json", response.header("Content-Type")),
                () -> Assertions.assertNotNull(response.body())
        );

        JSONAssert.assertEquals(account, response.body().string(), IGNORE_ID_COMPARATOR);
    }

    @Test
    void checkGetAccount() throws IOException, JSONException {
        Account account = new Account(12.99, "USD");
        accountStorageService.addAccount(account);
        final String accountJson = "{\"id\":\"" + account.getId() + "\",\"currency\":\"USD\",\"amount\":12.99}";

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/account/" + account.getId())
                .get()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertAll("assert http params",
                () -> Assertions.assertEquals(200, response.code()),
                () -> Assertions.assertEquals("application/json", response.header("Content-Type")),
                () -> Assertions.assertNotNull(response.body())
        );

        JSONAssert.assertEquals(accountJson, response.body().string(), false);
    }

    @Test
    void checkGetAccountByInvalidId() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/account/WRONG_ID")
                .get()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertAll("assert http params",
                () -> Assertions.assertEquals(404, response.code()),
                () -> Assertions.assertEquals("application/json", response.header("Content-Type")),
                () -> Assertions.assertNotNull(response.body())
        );
        JSONAssert.assertEquals("{\"Error\":\"Not found: Resource with id #WRONG_ID\"}", response.body().string(), false);
    }

    @Test
    void checkDeposit() throws IOException {
        Account account = new Account();
        accountStorageService.addAccount(account);

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/account/" + account.getId() + "/deposit/12.99/USD")
                .patch(RequestBody.create(null, new byte[]{}))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertAll("assert http params",
                () -> Assertions.assertEquals(200, response.code()),
                () -> Assertions.assertEquals("application/json", response.header("Content-Type")),
                () -> Assertions.assertNotNull(response.body())
        );
        Assertions.assertEquals("", response.body().string());
        Assertions.assertEquals(Money.of(12.99, "USD"), accountStorageService.getAccount(account.getId()).getAmount());
    }

    //TODO add negative tests
    // - invalid currency
    // - invalid account id
    // - invalid amount (0 , -1, more that account have)

    @Test
    void checkTransfer() throws IOException {
        Account accountFrom = new Account(100, "USD");
        Account accountTo = new Account();
        accountStorageService.addAccount(accountFrom);
        accountStorageService.addAccount(accountTo);

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/transfer/" + accountFrom.getId() + "/" + accountTo.getId() + "/50.99")
                .patch(RequestBody.create(null, new byte[]{}))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        Assertions.assertAll("assert http params",
                () -> Assertions.assertEquals(200, response.code()),
                () -> Assertions.assertEquals("application/json", response.header("Content-Type")),
                () -> Assertions.assertNotNull(response.body())
        );
        Assertions.assertEquals("", response.body().string());
        Assertions.assertEquals(Money.of(49.01, "USD"), accountStorageService.getAccount(accountFrom.getId()).getAmount());
        Assertions.assertEquals(Money.of(50.99, "USD"), accountStorageService.getAccount(accountTo.getId()).getAmount());
    }

}
