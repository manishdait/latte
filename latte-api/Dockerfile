FROM openjdk:17-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

FROM openjdk:17-alpine AS jre
WORKDIR /target
COPY --from=build app/target/latte-api.jar app.jar
RUN jar -xf app.jar
RUN jdeps --print-module-deps --ignore-missing-deps --multi-release 17 --class-path 'BOOT-INF/lib/*' app.jar > deps.info
RUN jlink --output jre --compress=2 --no-header-files --no-man-pages --add-modules "$(cat deps.info)"

FROM alpine
RUN apk update

WORKDIR /app
COPY --from=build app/target/latte-api.jar app.jar
COPY --from=jre target/jre jre

ARG PROFILE=dev

ENV DATASOURCE_USERNAME=postgres
ENV DATASOURCE_PASSWORD=password
ENV DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/latte_db
ENV PROFILE=${PROFILE}

EXPOSE 8080
ENTRYPOINT [ "jre/bin/java", "-jar", "app.jar" ]
