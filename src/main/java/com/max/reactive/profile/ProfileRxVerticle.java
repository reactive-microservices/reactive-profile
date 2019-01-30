package com.max.reactive.profile;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class ProfileRxVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String VERTEX_NAME = ProfileRxVerticle.class.getCanonicalName();

    private static final int PORT = 9090;

    private static final Map<String, String> profileIdToUsername = new HashMap<>();

    static {
        profileIdToUsername.put("1", "maksym");
        profileIdToUsername.put("2", "olesia");
    }

    private WebClient userClient;

    @Override
    public void start() {
        userClient = WebClient.create(vertx);
        Router router = Router.router(vertx);

        router.get("/profile/:id").handler(this::callUserService);

        vertx.createHttpServer().requestHandler(router::accept).listen(PORT);

        LOG.info("{} started at port {}", VERTEX_NAME, PORT);
    }


    private void callUserService(RoutingContext ctx) {

        String profileId = ctx.pathParam("id");
        String userName = profileIdToUsername.get(profileId);

        if( userName == null ){

            JsonObject errorData = new JsonObject();
            errorData.put("message", "Can't link profile id " + profileId + " to any user name.");

            ctx.response().setStatusCode(404).
                    putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                    end(errorData.encode());
        }

        HttpRequest<JsonObject> request = userClient.
                get(7070, "localhost", "/user/" + userName).
                as(BodyCodec.jsonObject());

        request.send(asyncResp -> {
            if (asyncResp.failed()) {
                JsonObject errorData = new JsonObject();
                errorData.put("message", "Failed to call reactive-user: " + asyncResp.cause().getMessage());

                ctx.response().
                        setStatusCode(500).
                        putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                        end(errorData.encode());
            }
            else {

                JsonObject userData = asyncResp.result().body();

                JsonObject data = new JsonObject();
                data.put("value", "profile-" + ctx.pathParam("id"));
                data.put("user", userData);

                ctx.response().
                        setStatusCode(200).
                        putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                        end(data.encode());
            }
        });
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", VERTEX_NAME);
    }
}
