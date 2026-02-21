import java.util.Properties

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.0" apply false
}

val localProperties = Properties()
file("local.properties").takeIf { it.exists() }?.inputStream()?.use { localProperties.load(it) }
