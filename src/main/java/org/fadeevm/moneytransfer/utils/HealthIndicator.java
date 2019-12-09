package org.fadeevm.moneytransfer.utils;

import spark.Request;
import spark.Response;

public interface HealthIndicator {
    Object healthCheck(Request request, Response response);
}
