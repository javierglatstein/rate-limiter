plugins {
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.0"
    id("io.micronaut.aot") version "4.4.0"
    id("org.jmailen.kotlinter") version "4.1.1"
}

version = "0.1"
group = "com.example"

//region dependencies versions
val awsSdkV1Version = "1.12.739"
val awsSdkV2Version = "2.20.162"
val ioMockkVersion = "1.13.8"
val jacksonModuleKotlinVersion = "2.14.2"
val junitJupiterVersion = "5.10.2"
val ksuidVersion = "1.1.2"
val okhttpVersion = "4.12.0"
val testcontainersBomVersion = "1.19.8"
val wireMockVersion = "3.0.1"
//endregion

val kotlinVersion = project.properties["kotlinVersion"]

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")

    // Micronaut
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("io.micronaut.jms:micronaut-jms-sqs")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")

    // AWS
    implementation("software.amazon.awssdk:bom:$awsSdkV2Version")
    implementation("software.amazon.awssdk:sdk-core")
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:dynamodb-enhanced")
    implementation("software.amazon.awssdk:sqs")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")

    // KSUID
    implementation("com.github.ksuid:ksuid:$ksuidVersion")

    // Squareup
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.yaml:snakeyaml")

    // Test
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("io.mockk:mockk:$ioMockkVersion")
    testImplementation("org.testcontainers:testcontainers-bom:$testcontainersBomVersion")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:localstack")
    testImplementation("com.amazonaws:aws-java-sdk-sqs:$awsSdkV1Version") {
        because("localstack uses aws sdk 1 - so it is needed to be added as a runtime dep")
    }
    testImplementation("com.github.tomakehurst:wiremock-jre8:$wireMockVersion")
}


application {
    mainClass = "com.example.ApplicationKt"
}

kotlin {
    jvmToolchain(17)
}

tasks {
    check {
        dependsOn("installKotlinterPrePushHook")
    }

    test {
        finalizedBy(jacocoTestReport)
    }
}


graalvmNative.toolchainDetection = false
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}
