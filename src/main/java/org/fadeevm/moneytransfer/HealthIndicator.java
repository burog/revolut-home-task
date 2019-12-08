package org.fadeevm.moneytransfer;

import spark.Request;
import spark.Response;

public interface HealthIndicator {
    Object healthCheck(Request request, Response response);
}
