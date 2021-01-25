group = "com.qianlei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":raft-core"))
    implementation("org.jline", "jline", "3.18.0")
    implementation("commons-cli", "commons-cli", "1.4")
}
