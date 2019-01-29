package com.max.reactive.user;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class AppMain {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private AppMain() {
        final Vertx vertex = Vertx.vertx();
        vertex.deployVerticle(new MainRxVerticle());
    }

    public static void main(String[] args) {
        try {
            new AppMain();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
