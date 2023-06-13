import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.example.chatptq"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.retrofit2:converter-gson:2.9.0")
                implementation("androidx.datastore:datastore-preferences-core:1.1.0-dev01")
//                implementation("com.mikepenz:multiplatform-markdown-renderer:0.6.1")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(/*TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, */TargetFormat.Exe)
            packageName = "ChatPTQ"
            packageVersion = "1.0.1"

            modules("java.instrument", "java.management", "java.naming", "java.sql", "jdk.unsupported")

            windows {
                packageVersion = "1.0.1"
                iconFile.set(project.file("launcher/184.ico"))
            }
        }

        buildTypes.release.proguard {
            obfuscate.set(false)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }

        javaHome = "D:\\Program Files(x86)\\JDK17\\jdk-17.0.7"
    }
}
