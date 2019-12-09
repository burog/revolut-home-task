package org.fadeevm.moneytransfer;


import okhttp3.*;
import org.fadeevm.moneytransfer.models.Account;
import org.fadeevm.moneytransfer.services.AccountStorageService;
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

        Assertions.assertEquals(response.code(), 200);
        Assertions.assertNotNull(response.body());
        Assertions.assertEquals(response.body().string(), "OK");
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
                () -> Assertions.assertEquals(response.code(), 201),
                () -> Assertions.assertEquals(response.header("Content-Type"), "application/json"),
                () -> Assertions.assertNotNull(response.body())
        );

        JSONAssert.assertEquals(response.body().string(), account, IGNORE_ID_COMPARATOR);
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
                () -> Assertions.assertEquals(response.code(), 200),
                () -> Assertions.assertEquals(response.header("Content-Type"), "application/json"),
                () -> Assertions.assertNotNull(response.body())
        );

        JSONAssert.assertEquals(response.body().string(), accountJson, false);
    }
}
