group = "com.qianlei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-protobuf", "1.0.1")
    implementation("io.github.microutils", "kotlin-logging", "1.12.0")
    implementation("org.slf4j", "slf4j-log4j12", "1.7.30")
    implementation("com.google.guava:guava:30.1-jre")
    implementation("io.netty", "netty-all", "4.1.58.Final")
}
