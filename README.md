# How to run

## Building from scratch

To run this project you need to follow these steps

1. Assuming you are using a linux based os, build the service with
    ```bash
    ./gradlew build 
    ```
2. Run local services

   ```bash
    docker compose -f docker-compose-services.yml up
    ```

3. Export local variables

    ```bash
    export MICRONAUT_ENVIRONMENTS=local
    ```

4. Start the service
    ```bash
    java -jar ./build/libs/rate-limiter-proxy-kotlin-0.1-all.jar
    ```

## Dockerized version

1. Start the service
    ```bash
    docker compose up
    ```
