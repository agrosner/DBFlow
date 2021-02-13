object Versions {
    const val Kotlin = "1.4.21"
    const val TargetSdk = 28
    const val MinSdk = 7
    const val MinSdkRX = 16
    const val SQLCipherMin = 7
    const val ArchMin = 14
}

object Dependencies {
    const val SqlCipher = "net.zetetic:android-database-sqlcipher:4.4.2"
    const val RX = "io.reactivex.rxjava3:rxjava:3.0.4"
    const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2"
    const val JavaPoet = "com.squareup:javapoet:1.11.1"
    const val KPoet = "com.github.agrosner:KPoet:1.0.0"
    const val JavaXAnnotation = "org.glassfish:javax.annotation:10.0-b28"
    const val JUnit = "junit:junit:4.12"

    object AndroidX {
        const val Annotations = "androidx.annotation:annotation:1.1.0"
        const val LiveData = "androidx.lifecycle:lifecycle-livedata:2.2.0"
        const val Paging = "androidx.paging:paging-runtime:2.1.2"
    }
}
