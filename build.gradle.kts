plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false

    // KSP version for Kotlin 2.1.0
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("io.objectbox") version "4.0.3" apply false
}