package org.fadeevm.moneytransfer.utils;

import org.eclipse.jetty.server.CustomRequestLog;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

public class Spark {
    public static void createServerWithRequestLog() {
        EmbeddedJettyFactory factory = createEmbeddedJettyFactoryWithRequestLog();
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, factory);
    }

    private static EmbeddedJettyFactory createEmbeddedJettyFactoryWithRequestLog() {
        CustomRequestLog requestLog = new RequestLogFactory().create();
        return new EmbeddedJettyFactoryConstructor(requestLog).create();
    }
}
