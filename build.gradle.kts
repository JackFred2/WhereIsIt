@file:Suppress("UnstableApiUsage", "RedundantNullableReturnType")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.ajoberstar.grgit.Grgit
import org.gradle.jvm.tasks.Jar
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask

plugins {
    id("maven-publish")
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("com.github.breadmoirai.github-release") version "2.4.1"
    id("org.ajoberstar.grgit") version "5.2.1"
    id("me.modmuss50.mod-publish-plugin") version "0.3.3"
}

val grgit: Grgit? = project.grgit
var canPublish = grgit != null && System.getenv("RELEASE") != null

fun getVersionSuffix(): String {
    return grgit?.branch?.current()?.name ?: "nogit+${properties["minecraft_version"]}"
}

group = properties["maven_group"]!!

if (System.getenv().containsKey("NEW_TAG")) {
    version = System.getenv("NEW_TAG").substring(1)
} else {
    val versionStr = "${properties["mod_version"]}+${properties["minecraft_version"]!!}"
    canPublish = false
    version = if (grgit != null) {
        "$versionStr+dev-${grgit.log()[0].abbreviatedId}"
    } else {
        "$versionStr+dev-nogit"
    }
}

repositories {
    mavenLocal()

    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
        content {
            includeGroup("org.parchmentmc.data")
        }
    }

    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroup("com.terraformersmc")
            includeGroup("dev.emi")
        }
    }

    maven {
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }

    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
        content {
            includeGroupAndSubgroups("me.shedaniel")
            includeGroup("dev.architectury")
        }
    }

    maven {
        name = "Xander Maven"
        url = uri("https://maven.isxander.dev/releases")
        content {
            includeGroupAndSubgroups("dev.isxander")
            includeGroupAndSubgroups("org.quiltmc")
        }
    }

    maven {
        name = "Xander Snapshot Maven"
        url = uri("https://maven.isxander.dev/snapshots")
        content {
            includeGroup("dev.isxander")
            includeGroupAndSubgroups("org.quiltmc")
        }
    }

    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

java {
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("whereisit") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    log4jConfigs.from(file("log4j2.xml"))

    runs.forEach {
        it.programArgs.addAll("--username JackFred".split(" "))
    }

    accessWidenerPath.set(file("src/main/resources/whereisit.accesswidener"))

    mixin {
        defaultRefmapName.set("whereisit.refmap.json")
    }
}

// ✅ Конфигурация для JackFredLib
val embedJackFredLib by configurations.creating {
    isTransitive = false
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    // ✅ JackFredLib - главный JAR из mavenLocal
    val jackfredlibVersion = properties["jackfredlib_version"]

    modCompileOnly("red.jackf.jackfredlib:jackfredlib:${jackfredlibVersion}")
    modLocalRuntime("red.jackf.jackfredlib:jackfredlib:${jackfredlibVersion}")
    embedJackFredLib("red.jackf.jackfredlib:jackfredlib:${jackfredlibVersion}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric-api_version"]}")

    modImplementation("dev.isxander:yet-another-config-lib:${properties["yacl_version"]}") {
        exclude(group = "com.terraformersmc", module = "modmenu")
    }

    modCompileOnly("com.terraformersmc:modmenu:${properties["modmenu_version"]}")
    modLocalRuntime("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

    modCompileOnly("maven.modrinth:jei:${properties["jei_modrinth_id"]}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${properties["rei_version"]}")
    modCompileOnly("dev.emi:emi-fabric:${properties["emi_version"]}")
}

tasks.withType<ProcessResources>().configureEach {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand(inputs.properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"

    options.compilerArgs.addAll(listOf(
        "-Xmaxerrs", "1000"
    ))

    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-AreobfTsrgFile=${project.projectDir}/.gradle/loom-cache/mixin-map-${properties["minecraft_version"]}.tsrg",
            "-AoutRefMapFile=${layout.buildDirectory.get()}/tmp/compileJava/whereisit.refmap.json",
            "-AdefaultObfuscationEnv=named:intermediary"
        )
    })
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val extractJackFredLib = tasks.register<Copy>("extractJackFredLib") {
    from({
        embedJackFredLib.resolve().map { mainJar ->
            // Распаковываем главный JAR
            val mainTree = zipTree(mainJar)

            // Ищем вложенные JAR'ы в META-INF/jars
            val metaInfJars = mainTree.matching {
                include("META-INF/jars/*.jar")
            }.files

            // Распаковываем вложенные JAR'ы
            metaInfJars.map { zipTree(it) } + mainTree
        }.flatten()
    })

    into(layout.buildDirectory.dir("jackfredlib-extracted"))

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/MANIFEST.MF",
        "META-INF/jars/**",
        "**/module-info.class",
        "fabric.mod.json"  // ✅ Исключаем fabric.mod.json JackFredLib
    )

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("jar") {
    dependsOn(extractJackFredLib)

    // ✅ Добавлена стратегия дубликатов для всего JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from("LICENSE") {
        rename { "${it}_${properties["archivesBaseName"]}"}
    }

    // ✅ Встраиваем распакованные классы JackFredLib
    from(extractJackFredLib.map { it.destinationDir }) {
        // Исключаем fabric.mod.json из JackFredLib - используем только свой
        exclude("fabric.mod.json")
    }

    doFirst {
        val refmapSrc = file("${layout.buildDirectory.get()}/tmp/compileJava/whereisit.refmap.json")
        val refmapDest = file("${layout.buildDirectory.get()}/resources/main/whereisit.refmap.json")

        if (refmapSrc.exists()) {
            refmapDest.parentFile.mkdirs()
            refmapSrc.copyTo(refmapDest, overwrite = true)
            println("✅ Copied refmap: ${refmapSrc.name}")
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]!!)
        }
    }

    repositories {
        if (!System.getenv().containsKey("CI")) repositories.mavenLocal()

        if (canPublish) {
            maven {
                name = "JackFredMaven"
                url = uri("https://maven.jackf.red/releases/")
                content {
                    includeGroupByRegex("red.jackf.*")
                }
                credentials {
                    username = properties["jfmaven.user"]?.toString() ?: System.getenv("JACKFRED_MAVEN_USER")
                    password = properties["jfmaven.key"]?.toString() ?: System.getenv("JACKFRED_MAVEN_PASS")
                }
            }
        }
    }
}

if (canPublish) {
    val lastTag = if (System.getenv("PREVIOUS_TAG") == "NONE") null else System.getenv("PREVIOUS_TAG")
    val newTag = "v$version"

    var generateChangelogTask: TaskProvider<GenerateChangelogTask>? = null

    if (lastTag != null) {
        val changelogHeader = if (properties.containsKey("changelogHeaderAddon")) {
            val addonProp: String = properties["changelogHeaderAddon"]!!.toString()
            if (addonProp.isNotBlank()) addonProp + "\n\n" else ""
        } else ""

        generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
            this.lastTag.set(lastTag)
            this.newTag.set(newTag)
            githubUrl.set(properties["github_url"]!!.toString())
            prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))

            prologue.set(changelogHeader + """
             |Bundled:
             |  - JackFredLib: ${properties["jackfredlib_version"]}
             |  """.trimMargin())
        }
    }

    val changelogTextProvider = generateChangelogTask?.let { task ->
        provider {
            task.get().changelogFile.get().asFile.readText()
        }
    } ?: provider { "No Changelog Generated" }

    tasks.named<GithubReleaseTask>("githubRelease") {
        generateChangelogTask?.let { dependsOn(it) }

        authorization = System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" }
        owner = properties["github_owner"]!!.toString()
        repo = properties["github_repo"]!!.toString()
        tagName = newTag
        releaseName = "${properties["mod_name"]} $newTag"
        targetCommitish = grgit!!.branch.current().name
        releaseAssets.from(
            tasks["remapJar"].outputs.files,
            tasks["remapSourcesJar"].outputs.files,
        )
        subprojects.forEach {
            releaseAssets.from(
                it.tasks["remapJar"].outputs.files,
                it.tasks["remapSourcesJar"].outputs.files,
            )
        }

        body = changelogTextProvider
    }

    if (listOf("CURSEFORGE_TOKEN", "MODRINTH_TOKEN").any { System.getenv().containsKey(it) }) {
        publishMods {
            changelog.set(changelogTextProvider)
            type.set(when(properties["release_type"]) {
                "release" -> ReleaseType.STABLE
                "beta" -> ReleaseType.BETA
                else -> ReleaseType.ALPHA
            })
            modLoaders.add("fabric")
            modLoaders.add("quilt")
            file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)

            if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
                curseforge {
                    projectId.set("378036")
                    accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
                    properties["game_versions_curse"]!!.toString().split(",").forEach {
                        minecraftVersions.add(it)
                    }
                    displayName.set("${properties["prefix"]!!} ${properties["mod_name"]!!} ${version.get()}")
                    listOf("fabric-api", "yacl").forEach {
                        requires { slug.set(it) }
                    }
                    listOf("emi", "jei", "roughly-enough-items", "modmenu").forEach {
                        optional { slug.set(it) }
                    }
                }
            }

            if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
                modrinth {
                    accessToken.set(System.getenv("MODRINTH_TOKEN"))
                    projectId.set("FCTyEqkn")
                    properties["game_versions_mr"]!!.toString().split(",").forEach {
                        minecraftVersions.add(it)
                    }
                    displayName.set("${properties["mod_name"]!!} ${version.get()}")
                    listOf("fabric-api", "yacl").forEach {
                        requires { slug.set(it) }
                    }
                    listOf("emi", "jei", "rei", "modmenu").forEach {
                        optional { slug.set(it) }
                    }
                }
            }
        }
    }
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
    mcVersion.set(properties["minecraft_version"]!!.toString())
    loader.set("fabric")
}