object Versions {
    const val Kotlin = "1.6.0"
    const val TargetSdk = 31
    const val MinSdk = 21
    const val MinSdkRX = 21
    const val SQLCipherMin = 21
    const val ArchMin = 21
    const val KSP = "${Kotlin}-1.0.2"
}

object Dependencies {
    const val SqlCipher = "net.zetetic:android-database-sqlcipher:4.5.0"
    const val RX = "io.reactivex.rxjava3:rxjava:3.1.3"
    const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2"
    const val JavaPoet = "com.squareup:javapoet:1.13.0"
    const val KPoet = "com.github.agrosner:KPoet:1.0.0"
    const val JavaXAnnotation = "org.glassfish:javax.annotation:10.0-b28"
    const val JUnit = "junit:junit:4.12"
    const val KotlinPoet = "com.squareup:kotlinpoet-ksp:1.10.2"
    const val KotlinPoetJavaPoetInterop = "com.squareup:kotlinpoet-javapoet:1.10.2"
    const val KSP = "com.google.devtools.ksp:symbol-processing-api:${Versions.KSP}"
    const val Koin = "io.insert-koin:koin-core:3.1.4"
    const val KoinTest = "io.insert-koin:koin-test-junit4:3.1.4"
    const val MockitoKotlin = "org.mockito.kotlin:mockito-kotlin:4.0.0"

    const val KotlinCompileTestingKSP = "com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.6"


    object AndroidX {
        const val Annotations = "androidx.annotation:annotation:1.3.0"
        const val LiveData = "androidx.lifecycle:lifecycle-livedata:2.4.0"
        const val Paging = "androidx.paging:paging-runtime:2.1.2"
    }
}
