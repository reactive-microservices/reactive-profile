package com.max.reactive.profile;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProfileRxVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String VERTEX_NAME = ProfileRxVerticle.class.getCanonicalName();

    private static final int PORT = 9090;

    private static final Map<String, List<String>> profileIdToUsername = new HashMap<>();

    static {
        profileIdToUsername.put("1", Arrays.asList("maksym", "olesia"));

        profileIdToUsername.put("2", Arrays.asList("maksym", "unknown-user-123"));

        profileIdToUsername.put("3", IntStream.range(0, 50).
                mapToObj(value -> "other-" + value).
                collect(Collectors.toList()));
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.get("/health").handler(request -> {
            JsonObject data = new JsonObject();
            data.put("service_name", "reactive_profile");
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

        Observable<JsonObject> userObs = Observable.from(allUserNames).
                flatMap(singleUserName -> bus.
                        rxSend("reactive-user/user", singleUserName).
                        timeout(1, TimeUnit.SECONDS).
                        map(msg -> (JsonObject) msg.body()).
                        onErrorReturn(err -> {
                            JsonObject userData = new JsonObject();
                            userData.put("errorMessage", err.getMessage());
                            return userData;
                        }).
                        toObservable());

        Observable<JsonArray> usersArrObs = userObs.collect(JsonArray::new, JsonArray::add);

        Observable<JsonObject> profileObs = usersArrObs.map(usersArray -> {
            JsonObject profileData = new JsonObject();
            profileData.put("value", "profile-" + profileId);
            profileData.put("users", usersArray);
            return profileData;
        });

        profileObs.subscribe(fullProfileData -> onSuccessProfile(fullProfileData, ctx),
                             error -> onErrorProfile(error, ctx));
    }

    private void profileNotFound(String profileId, RoutingContext ctx) {
        JsonObject errorData = new JsonObject();
        errorData.put("message", "Can't find profile with id " + profileId + ".");

        ctx.response().setStatusCode(404).
                putHeader("Content-Type", "application/json").
                end(errorData.encode());
    }

    private static void onSuccessProfile(JsonObject fullProfileData, RoutingContext ctx) {
        ctx.response().
                setStatusCode(200).
                putHeader("Content-Type", "application/json").
                end(fullProfileData.encodePrettily());
    }

    private static void onErrorProfile(Throwable error, RoutingContext ctx) {
        LOG.error("Error obtaining user data", error);
        ctx.response().
                setStatusCode(500).
                putHeader("Content-Type", "application/json").
                end(error.getMessage());
    }


    @Override
    public void stop() {
        LOG.info("{} stopped", VERTEX_NAME);
    }
}
