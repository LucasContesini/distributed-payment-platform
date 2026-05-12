plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "com.finflow"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    }
}
