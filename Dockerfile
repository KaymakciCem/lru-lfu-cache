FROM openjdk:17

VOLUME /tmp

ADD ./target/java-engineer-distributed-systems-challenge-1.0-SNAPSHOT.jar caching-systems-service.jar

ENTRYPOINT ["java","-jar","caching-systems-service.jar"]