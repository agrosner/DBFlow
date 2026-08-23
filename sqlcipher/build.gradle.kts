plugins {
    id("androidConfig")
}

android {
    namespace = "com.dbflow5.sqlcipher"
    defaultConfig {
        minSdk = Versions.SQLCipherMin
    }
}

configureJdk("com.dbflow5.annotation.opts.InternalDBFlowApi")

dependencies {
    api(libs.sqlCipher)
    api(libs.androidx.sqlite)
    api(project(":lib"))
}
