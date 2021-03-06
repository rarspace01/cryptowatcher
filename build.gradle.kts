import java.io.ByteArrayOutputStream

plugins {
    val kotlinVersion = "1.6.10"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion

    id("io.quarkus")
}

group = "com.cryptowatcher"
version = "1.0.0"

object Versions {
    const val kotlinxCoroutinesVersion = "1.6.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-mongodb-panache")
    implementation("io.quarkus:quarkus-mongodb-panache-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.kotlinxCoroutinesVersion}"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        this.jvmTarget = JavaVersion.VERSION_11.toString()
        this.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        this.javaParameters = true
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

fun getCheckedOutGitCommitHash(): String {
    val gitFolder = "$projectDir/.git/"
    val takeFromHash = 12
    /*
     * '.git/HEAD' contains either
     *      in case of detached head: the currently checked out commit hash
     *      otherwise: a reference to a file containing the current commit hash
     */
    val head = File(gitFolder + "HEAD").readText(Charsets.UTF_8).split(":") // .git/HEAD
    val isCommit = head.size == 1 // e5a7c79edabbf7dd39888442df081b1c9d8e88fd
    // def isRef = head.length > 1     // ref: refs/heads/master

    if (isCommit) return head[0].trim().take(takeFromHash) // e5a7c79edabb

    val refHead = File(gitFolder + head[1].trim()) // .git/refs/heads/master
    return refHead.readText(Charsets.UTF_8).trim().take(takeFromHash)
}

fun runCommand(commands: List<String>, currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = commands
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

fun getTagVersion(): String {
    return runCommand(listOf("sh", "-c", "git fetch --all --tags>/dev/null&& git describe --tags --always --first-parent|tail -1")).trim()
}

tasks.register("createProperties") {
    dependsOn("processResources")
    doLast {
        val writer = file("$buildDir/resources/main/version.properties").writer(Charsets.UTF_8)
        val deductedVersion = getTagVersion() + "-" + getCheckedOutGitCommitHash()
        println("Wrote [Version:$deductedVersion] to \"$buildDir/resources/main/version.properties\"")
        writer.write("version=" + deductedVersion)
        writer.flush()
        writer.close()
    }
}

tasks.named("classes") {
    dependsOn("createProperties")
}
