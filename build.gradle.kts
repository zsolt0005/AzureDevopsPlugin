plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.22"
    id("org.jetbrains.intellij") version "1.11.0"
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.formdev:flatlaf:3.0")
    implementation("com.formdev:flatlaf-extras:3.0")
    implementation("org.commonmark:commonmark:0.21.0")
}

group = "com.sportisimo.devops"
version = "1.13.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2022.2")
    type.set("IC")

    plugins.set(listOf("git4idea"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
