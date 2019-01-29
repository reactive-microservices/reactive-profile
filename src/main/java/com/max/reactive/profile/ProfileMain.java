package com.max.reactive.profile;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class ProfileMain {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ProfileMain() {
        final Vertx vertex = Vertx.vertx();
        vertex.deployVerticle(new ProfileRxVerticle());
    }

    public static void main(String[] args) {
        try {
            new ProfileMain();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
