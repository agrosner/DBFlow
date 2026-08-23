plugins {
    id("androidConfig")
}

android {
    namespace = "com.dbflow5.reactivestreams"
    defaultConfig {
        minSdk = Versions.MinSdkRX
    }
}

configureJdk("com.dbflow5.annotation.opts.InternalDBFlowApi")

dependencies {
    api(project(":lib"))
    api(libs.rx)
}
