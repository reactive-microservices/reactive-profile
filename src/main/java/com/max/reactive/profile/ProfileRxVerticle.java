package com.max.reactive.profile;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileRxVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String VERTEX_NAME = ProfileRxVerticle.class.getCanonicalName();

    private static final int PORT = 9090;

    private static final Map<String, List<String>> profileIdToUsername = new HashMap<>();

    static {
        profileIdToUsername.put("1", Arrays.asList("maksym", "olesia"));
        profileIdToUsername.put("2", Arrays.asList("zorro", "other"));
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.get("/user/health").handler(request -> {
            JsonObject data = new JsonObject();
            data.put("status", "healthy");
            request.response().
                    putHeader("Content-Type", "application/json").
                    end(data.encode());
        });

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

        EventBus bus = vertx.eventBus();

        Single<JsonObject> req1 =
                bus.rxSend("reactive-user/user", "maksym").
                        map(msg -> (JsonObject) msg.body());

        Single<JsonObject> req2 =
                bus.rxSend("reactive-user/user", "olesia").
                        map(msg -> (JsonObject) msg.body());

        Single.zip(req1, req2, (userData1, userData2) -> {
            JsonArray usersArray = new JsonArray();
            usersArray.add(userData1);
            usersArray.add(userData2);
            return usersArray;
        }).
                subscribe(usersData -> {
                              JsonObject profileData = new JsonObject();
                              profileData.put("value", "profile-" + ctx.pathParam("id"));
                              profileData.put("users", usersData);

                              ctx.response().
                                      setStatusCode(200).
                                      putHeader("Content-Type", "application/json").
                                      end(profileData.encode());
                          },
                          err -> {
                              LOG.error("Error obtaining user data", err);
                              ctx.response().
                                      setStatusCode(500).
                                      putHeader("Content-Type", "application/json").
                                      end(err.getMessage());
                          });


//        Observable.from(allUserNames).
//                map(singleUserName -> userClient.get(7070, "localhost", "/user/" + singleUserName).as(BodyCodec.jsonObject
//                ())).
//                flatMap(httpReq ->
//                                httpReq.rxSend().
//                                        subscribeOn(Schedulers.io()).
//                                        map(HttpResponse::body).
//                                        toObservable()).
//                collect(JsonArray::new, JsonArray::add).
//                map(usersArray -> {
//                    JsonObject profileData = new JsonObject();
//                    profileData.put("value", "profile-" + ctx.pathParam("id"));
//                    profileData.put("users", usersArray);
//                    return profileData;
//                }).
//                subscribe(fullProfileData -> onSuccess(fullProfileData, ctx),
//                          error -> onError(error, ctx));

    }

    private void profileNotFound(String profileId, RoutingContext ctx) {
        JsonObject errorData = new JsonObject();
        errorData.put("message", "Can't find profile with id " + profileId + ".");

        ctx.response().setStatusCode(404).
                putHeader("Content-Type", "application/json").
                end(errorData.encode());
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", VERTEX_NAME);
    }
}
