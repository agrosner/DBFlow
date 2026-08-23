plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.dbflow5"
version = "5.0.0-alpha2"

gradlePlugin {
    plugins {
        create("dbflow") {
            id = "com.dbflow5"
            implementationClass = "com.dbflow5.gradle.DBFlowGradlePlugin"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
}
