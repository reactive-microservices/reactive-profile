# Reactive profile microservice example using Vert.x and RxJava.


## Unit tests.

```
mvn clean test
```

## Integration tests

```
mvn clean test -P integration
```

## Run locally

Using maven:
```
mvn compile vertx:run -Dvertx.runArgs="-cluster -Djava.net.preferIPv4Stack=true"
```
Using fat jar file:
```
java -jar target/reactive-profile-1.0.0-SNAPSHOT.jar --cluster -Djava.net.preferIPv4Stack=true
```

Check service running:
```
curl localhost:9090
```

## Related project

https://github.com/redhat-developer/reactive-microservices-in-java
