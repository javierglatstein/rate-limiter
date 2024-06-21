FROM gradle:8.8.0-jdk17 AS builder

WORKDIR /build

# Only copy dependency-related files
COPY build.gradle.kts gradle.properties /build/

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

COPY ./ ./
RUN gradle clean assemble --no-daemon

FROM gradle:8.8.0-jdk17 AS runner
COPY --from=builder /build/build/libs/rate-limiter-proxy-kotlin-*-all.jar /service/service.jar

EXPOSE 8080

CMD ["java", "-jar", "/service/service.jar"]
