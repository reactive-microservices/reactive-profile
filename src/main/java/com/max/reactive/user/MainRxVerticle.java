package com.max.reactive.user;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.LongAdder;

public class MainRxVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String VERTEX_NAME = MainRxVerticle.class.getCanonicalName();

    private static final LongAdder REQUESTS_COUNTER = new LongAdder();

    private static final int PORT = 7070;

    @Override
    public void start() {

        HttpServer server = vertx.createHttpServer();

        server.requestStream().toObservable().subscribe(request -> {
            REQUESTS_COUNTER.increment();
            request.response().end("Hello from Vert.x with id " + REQUESTS_COUNTER +
                                           " [" + Thread.currentThread().getName() + "]");
        });

        server.rxListen(PORT).subscribe();

        LOG.info("{} started at port {}", VERTEX_NAME, PORT);
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", VERTEX_NAME);
    }
}
