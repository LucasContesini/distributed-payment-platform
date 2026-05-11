plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    java
}

dependencies {
    implementation(project(":shared-libs:common-domain"))
    implementation(project(":shared-libs:common-events"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.prometheus)

    testImplementation(libs.spring.boot.starter.test)
}
