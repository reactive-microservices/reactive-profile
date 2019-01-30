package com.max.reactive.profile;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ProfileRxVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String VERTEX_NAME = ProfileRxVerticle.class.getCanonicalName();

    private static final int PORT = 9090;

    private static final Map<String, List<String>> profileIdToUsername = new HashMap<>();

    static {
        profileIdToUsername.put("1", Arrays.asList("maksym", "olesia"));
    }

    private WebClient userClient;

    @Override
    public void start() {
        userClient = WebClient.create(vertx);
        Router router = Router.router(vertx);

        router.get("/profile/:id").handler(this::gatherProfileInformation);

        vertx.createHttpServer().
                requestHandler(router::accept).
                listen(PORT);

        LOG.info("{} started at port {}", VERTEX_NAME, PORT);
    }


    private void gatherProfileInformation(RoutingContext ctx) {

        String profileId = ctx.pathParam("id");
        List<String> allUserNames = profileIdToUsername.get(profileId);

        if (allUserNames == null) {
            profileNotFound(profileId, ctx);
            return;
        }

        AtomicLong handledUsersCnt = new AtomicLong();
        AtomicLong failedCnt = new AtomicLong();

        JsonArray usersData = new JsonArray();

        for (String userName : allUserNames) {

            HttpRequest<JsonObject> request = userClient.
                    get(7070, "localhost", "/user/" + userName).
                    as(BodyCodec.jsonObject());

            request.send(asyncResp -> {

                             handledUsersCnt.incrementAndGet();

                             if (asyncResp.failed() || asyncResp.result().statusCode() != 200) {
                                 failedCnt.incrementAndGet();
                             }
                             else {
                                 usersData.add(asyncResp.result().body());
                             }

                             // check if last response and write reply
                             if (handledUsersCnt.get() == allUserNames.size()) {

                                 //-----

                                 if (failedCnt.get() != 0) {
                                     JsonObject errorData = new JsonObject();
                                     errorData.put("message", "Failed to obtaine profile");

                                     ctx.response().
                                             setStatusCode(500).
                                             putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                                             end(errorData.encode());
                                 }
                                 else {

                                     JsonObject profileData = new JsonObject();
                                     profileData.put("value", "profile-" + ctx.pathParam("id"));
                                     profileData.put("users", usersData);

                                     ctx.response().
                                             setStatusCode(200).
                                             putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                                             end(profileData.encode());
                                 }

                                 //-----

                             }
                         }
            );
        }
    }

    private void profileNotFound(String profileId, RoutingContext ctx) {
        JsonObject errorData = new JsonObject();
        errorData.put("message", "Can't find profile with id " + profileId + ".");

        ctx.response().setStatusCode(404).
                putHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                end(errorData.encode());
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", VERTEX_NAME);
    }
}
