import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm") version "1.4.31"
}

group = "me.david"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral();
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test-junit"));
    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.4");
    implementation("com.google.code.gson:gson:2.8.6")
    implementation ("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
