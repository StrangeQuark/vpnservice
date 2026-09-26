FROM eclipse-temurin:21-alpine AS builder

WORKDIR /vpnservice

COPY pom.xml ./
RUN apk add --no-cache maven && mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-alpine

RUN apk add --no-cache curl
RUN addgroup -S -g 1000 msinit && adduser -S -u 1000 -G msinit msinit

WORKDIR /vpnservice

COPY --chown=msinit:msinit --from=builder /vpnservice/target/*.jar vpnservice.jar

ENV JAVA_OPTS=""

EXPOSE 6040

USER msinit
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar vpnservice.jar"]
