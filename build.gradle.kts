import org.ajoberstar.grgit.Grgit
import org.kohsuke.github.GitHub
import org.kohsuke.github.GHReleaseBuilder
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import com.modrinth.minotaur.TaskModrinthUpload
import com.modrinth.minotaur.request.VersionType

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:${project.property("github_api_version") as String}")
    }
}

plugins {
    id("maven-publish")
    id("fabric-loom")
    id("org.ajoberstar.grgit")
    id("com.matthewprenger.cursegradle")
    id("com.modrinth.minotaur")
}

operator fun Project.get(property: String): String {
    return property(property) as String
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


version = project["mod_version"]
group = project["maven_group"]

val environment: Map<String, String> = System.getenv()
val releaseName = "${name.split("-").joinToString(" ") { it.capitalize() }} ${(version as String).split("+")[0]}"
val releaseType = (version as String).split("+")[0].split("-").let { if(it.isNotEmpty()) if(it[1] == "BETA" || it[1] == "ALPHA") it[1] else "ALPHA" else "RELEASE" }
val releaseFile = "${buildDir}/libs/${base.archivesName.get()}-${version}.jar"
val cfGameVersion = (version as String).split("+")[1].let{ if(!project["minecraft_version"].contains("-") && project["minecraft_version"].startsWith(it)) project["minecraft_version"] else "$it-Snapshot"}

fun getChangeLog(): String {
    return "A changelog can be found at https://github.com/lucaargolo/$name/commits/"
}

fun getBranch(): String {
    environment["GITHUB_REF"]?.let { branch ->
        return branch.substring(branch.lastIndexOf("/") + 1)
    }
    val grgit = try {
        extensions.getByName("grgit") as Grgit
    }catch (ignored: Exception) {
        return "unknown"
    }
    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases")
    }
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:${project["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${project["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project["fabric_version"]}")

    modImplementation("com.terraformersmc:modmenu:${project["modmenu_version"]}")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand(mutableMapOf("version" to project.version))
    }

    from(sourceSets["main"].resources.srcDirs) {
        exclude("fabric.mod.json")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

java {
    withSourcesJar()
}

tasks.jar {
    from("LICENSE")
}

//Github publishing
task("github") {
    dependsOn(tasks.remapJar)
    group = "upload"

    onlyIf { environment.containsKey("GITHUB_TOKEN") }

    doLast {
        val github = GitHub.connectUsingOAuth(environment["GITHUB_TOKEN"])
        val repository = github.getRepository(environment["GITHUB_REPOSITORY"])

        val releaseBuilder = GHReleaseBuilder(repository, version as String)
        releaseBuilder.name(releaseName)
        releaseBuilder.body(getChangeLog())
        releaseBuilder.commitish(getBranch())

        val ghRelease = releaseBuilder.create()
        ghRelease.uploadAsset(file(releaseFile), "application/java-archive")
    }
}

//Curseforge publishing
curseforge {
    environment["CURSEFORGE_API_KEY"]?.let { apiKey = it }

    project(closureOf<CurseProject> {
        id = project["curseforge_id"]
        changelog = getChangeLog()
        releaseType = this@Build_gradle.releaseType.toLowerCase()
        addGameVersion(cfGameVersion)
        addGameVersion("Fabric")

        mainArtifact(file(releaseFile), closureOf<CurseArtifact> {
            displayName = releaseName
            relations(closureOf<CurseRelation> {
                requiredDependency("fabric-api")
            })
        })

        afterEvaluate {
            uploadTask.dependsOn("remapJar")
        }

    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}


modrinth {
    tasks.remapJar
    token.set(environment["MODRINTH_TOKEN"]) // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId.set(project["modrinth_id"]) // This can be the project ID or the slug. Either will work!
    versionNumber.set(version as String) // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType.set(releaseType.toLowerCase()) // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(file(releaseFile)) // With Loom, this MUST be set to `remapJar` instead of `jar`!
    gameVersions.add(project["minecraft_version"]) // Must be an array, even with only one version
    loaders.add("fabric") // Must also be an array - no need to specify this if you're using Loom or ForgeGradle
    dependencies { // A special DSL for creating dependencies
        required.project("fabric-api") // Creates a new required dependency on Fabric API
    }
    group = "upload"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}