object Versions {
    const val Kotlin = "1.6.10"
    const val TargetSdk = 31
    const val MinSdk = 21
    const val MinSdkRX = 21
    const val SQLCipherMin = 21
    const val ArchMin = 21
    const val KSP = "${Kotlin}-1.0.4"
    const val KotlinCompileTesting = "1.4.7"
    const val KotlinPoet = "1.10.2"
    const val Koin = "3.1.5"
    const val AtomicFu = "0.17.1"
}

object Dependencies {
    const val SqlCipher = "net.zetetic:android-database-sqlcipher:4.5.0"
    const val RX = "io.reactivex.rxjava3:rxjava:3.1.3"
    const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0"
    const val CoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0"
    const val JavaPoet = "com.squareup:javapoet:1.13.0"
    const val JavaXAnnotation = "org.glassfish:javax.annotation:10.0-b28"
    const val JUnit = "junit:junit:4.12"

    const val KotlinPoet = "com.squareup:kotlinpoet-ksp:${Versions.KotlinPoet}"
    const val KotlinPoetJavaPoetInterop = "com.squareup:kotlinpoet-javapoet:${Versions.KotlinPoet}"
    const val KotlinPoetMetadata = "com.squareup:kotlinpoet-metadata:${Versions.KotlinPoet}"

    const val KSP = "com.google.devtools.ksp:symbol-processing-api:${Versions.KSP}"
    const val Koin = "io.insert-koin:koin-core:${Versions.Koin}"
    const val KoinTest = "io.insert-koin:koin-test-junit4:${Versions.Koin}"
    const val MockitoKotlin = "org.mockito.kotlin:mockito-kotlin:4.0.0"

    const val KotlinCompileTesting =
        "com.github.tschuchortdev:kotlin-compile-testing:${Versions.KotlinCompileTesting}"
    const val KotlinCompileTestingKSP =
        "com.github.tschuchortdev:kotlin-compile-testing-ksp:${Versions.KotlinCompileTesting}"

    const val Turbine = "app.cash.turbine:turbine:0.7.0"

    const val AtomicFU = "org.jetbrains.kotlinx:atomicfu:${Versions.AtomicFu}"

    const val SQLiteJDBC = "org.xerial:sqlite-jdbc:3.36.0.3"
    const val HikariCP = "com.zaxxer:HikariCP:5.0.1"
    const val SLF4JApi = "org.slf4j:slf4j-api:1.7.36"
    const val SLF4JSimple = "org.slf4j:slf4j-simple:1.7.36"

    object AndroidX {
        const val Annotations = "androidx.annotation:annotation:1.3.0"
        const val LiveData = "androidx.lifecycle:lifecycle-livedata:2.4.0"
        const val Paging = "androidx.paging:paging-runtime:2.1.2"
    }
}
