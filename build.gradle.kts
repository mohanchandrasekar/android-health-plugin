plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("androidHealthPlugin") {
            id = "com.android.healthcheck"
            implementationClass = "com.android.healthcheck.AndroidHealthPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.yaml:snakeyaml:2.2")
}
