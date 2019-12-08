package org.fadeevm.moneytransfer.utils;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;

public class RequestLogFactory {

    public RequestLogFactory() {
    }

    CustomRequestLog create() {
        Slf4jRequestLogWriter slf4jRequestLogWriter = new Slf4jRequestLogWriter();
        return new CustomRequestLog(slf4jRequestLogWriter, CustomRequestLog.EXTENDED_NCSA_FORMAT);
    }
}