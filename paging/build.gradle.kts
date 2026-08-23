plugins {
    id("androidConfig")
}

android {
    namespace = "com.dbflow5.paging"
    defaultConfig {
        minSdk = Versions.ArchMin
    }
}

configureJdk("com.dbflow5.annotation.opts.InternalDBFlowApi")

dependencies {
    implementation(project(":lib"))
    api(libs.androidx.paging)
}
