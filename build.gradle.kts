plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.github.mohanchandrasekar"
version = "0.1.0"

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("androidHealthPlugin") {
            // This is what app projects will use:
            id = "com.android.healthcheck"
            implementationClass = "com.android.healthcheck.AndroidHealthPlugin"
        }
    }
}

dependencies {
    // Kotlin stdlib is pulled in by kotlin-dsl, no need to declare explicitly.
    implementation("org.yaml:snakeyaml:2.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "android-health-plugin"
            version = version.toString()
        }
    }
}
