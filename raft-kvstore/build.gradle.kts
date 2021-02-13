group = "com.qianlei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":raft-core"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-protobuf", "1.0.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.0.1")
    implementation("com.charleskorn.kaml", "kaml", "0.27.0")
    implementation("io.github.microutils", "kotlin-logging", "1.12.0")
    implementation("org.slf4j", "slf4j-log4j12", "1.7.30")
    implementation("com.google.guava", "guava", "30.1-jre")
    implementation("io.netty", "netty-all", "4.1.58.Final")
    implementation("io.ktor", "ktor-server-core", "1.5.1")
    implementation("io.ktor", "ktor-server-netty", "1.5.1")
    implementation("io.ktor", "ktor-serialization", "1.5.1")
    testImplementation("io.ktor", "ktor-server-tests", "1.5.1")
    implementation("org.rocksdb", "rocksdbjni", "6.15.2")
}
