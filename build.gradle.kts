import java.net.URI
import java.util.*
import org.gradle.api.file.DuplicatesStrategy

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    if (!file(filename).exists()) {
        return properties
    }
    file(filename).inputStream().use { properties.load(it) }
    return properties
}

plugins {
    id("java")
    id("maven-publish")
}

group = "gg.auroramc"
version = "2.2.0"

val includeExternalHooks = providers.gradleProperty("includeExternalHooks")
    .map(String::toBoolean)
    .orElse(false)
    .get()

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.auroramc.gg/releases/")
    maven("https://repo.auroramc.gg/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    // Quests (pikamug)
    maven("https://repo.codemc.io/repository/maven-public/")
    // BetonQuest (2)
    maven("https://nexus.betonquest.org/repository/betonquest/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("gg.auroramc:Aurora:2.4.0")
    compileOnly("gg.auroramc:AuroraQuests:2.0.0")

    if (includeExternalHooks) {
        // Quests
        compileOnly("me.pikamug.quests:quests-core:5.1.4")
        // Quests (LMBishop)
        compileOnly(name = "Quests-3.15.2-lmbishop", group = "com.leonardobishop", version = "3.15.2")
        // BetonQuest (2)
        compileOnly("org.betonquest:betonquest:2.1.3") {
            exclude("com.comphenix.packetwrapper")
        }
        compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
        // ItemsAdder
        compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
        // HeadDatabase
        compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
        // Jobs
        compileOnly(name = "Jobs5.2.4.6", group = "com.github.Zrips", version = "5.2.4.6")
        // AdvancedEnchantments
        compileOnly(name = "AdvancedEnchantments-8.7.4", group = "net.advancedplugins", version = "8.7.4")
    }

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveFileName.set("AuroraCrafting-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/INDEX.LIST")
}

sourceSets {
    main {
        java {
            if (!includeExternalHooks) {
                exclude("gg/auroramc/crafting/hooks/advancedenchantments/**")
                exclude("gg/auroramc/crafting/hooks/betonquests/**")
                exclude("gg/auroramc/crafting/hooks/hdb/**")
                exclude("gg/auroramc/crafting/hooks/itemsadder/**")
                exclude("gg/auroramc/crafting/hooks/jobsreborn/**")
                exclude("gg/auroramc/crafting/hooks/mythicmobs/**")
                exclude("gg/auroramc/crafting/hooks/quests/**")
                exclude("gg/auroramc/crafting/hooks/quests2/**")
            }
        }
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks {
    build {
        dependsOn("apiJar")
    }
}

tasks.register<Jar>("apiJar") {
    archiveBaseName.set("AuroraCraftingAPI")

    from(sourceSets.main.get().output) {
        include("gg/auroramc/crafting/api/**")
    }
}

val publishing = loadProperties("publish.properties")

publishing {
    repositories {
        maven {
            name = "AuroraMC"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI.create("https://repo.auroramc.gg/snapshots/")
            } else {
                URI.create("https://repo.auroramc.gg/releases/")
            }
            credentials {
                username = publishing.getProperty("username")
                password = publishing.getProperty("password")
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        groupId = "gg.auroramc"
        artifactId = "AuroraCraftingAPI"
        version = project.version.toString()

        artifact(tasks.named("apiJar"))
    }
}
