FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /workspace

COPY . .

ARG MODULE=app
RUN gradle --no-daemon :shared:build :${MODULE}:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app

ARG MODULE=app
ENV MODULE=${MODULE}

COPY --from=builder /workspace/${MODULE}/build/install/${MODULE}/ /app/

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "bin/${MODULE}"]
