@file:Suppress("UnstableApiUsage")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import java.net.URI

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.3-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.0.+"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
}

group = properties["maven_group"]!!
version = properties["mod_version"] ?: "dev"

val modReleaseType = properties["type"]?.toString() ?: "release"

base {
	archivesName.set("${properties["archives_base_name"]}")
}

repositories {
	// Parchment Mappings
	maven {
		name = "ParchmentMC"
		url = URI("https://maven.parchmentmc.org")
		content {
			includeGroup("org.parchmentmc.data")
		}
	}

	// Mod Menu, EMI
	maven {
		name = "TerraformersMC"
		url = URI("https://maven.terraformersmc.com/releases/")
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	// JEI
	maven {
		name = "Jared"
		url = URI("https://maven.blamejared.com/")
		content {
			includeGroup("mezz.jei")
		}
	}

	// REI
	maven {
		name = "Shedaniel"
		url = URI("https://maven.shedaniel.me")
		content {
			includeGroupByRegex("me.shedaniel.*")
			includeGroup("dev.architectury")
		}
	}

	// YACL
	maven {
		name = "Xander Maven"
		url = URI("https://maven.isxander.dev/releases")
		content {
			includeGroup("dev.isxander.yacl")
		}
	}

	// YACL Dependencies
	maven {
		name = "Sonatype"
		url = URI("https://oss.sonatype.org/content/repositories/snapshots")
		content {
			includeGroupByRegex("com.twelvemonkeys.*")
		}
	}

	// JEI, JSST
	maven {
		name = "Modrinth"
		url = URI("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	// JackFredLib
	maven {
		name = "GitHubPackages"
		url = URI("https://maven.pkg.github.com/JackFred2/JackFredLib")
		content {
			includeGroup("red.jackf")
		}
		credentials {
			username = properties["gpr.user"]?.toString() ?: System.getenv("GPR_USER")
			password = properties["gpr.key"]?.toString() ?: System.getenv("GPR_TOKEN")
		}
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

loom {
    splitEnvironmentSourceSets()

	mods {
		create("whereisit") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	runConfigs {
		configureEach {
			val path = buildscript.sourceFile?.parentFile?.resolve("log4j2.xml")
			path?.let { property("log4j2.configurationFile", path.path) }
		}
	}

	accessWidenerPath.set(file("src/main/resources/whereisit.accesswidener"))
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

	include("red.jackf:jackfredlib:${properties["jackfredlib_version"]}")
	modImplementation("red.jackf:jackfredlib:${properties["jackfredlib_version"]}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

	// Config
	modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:${properties["yacl_version"]}")
	implementation("blue.endless:jankson:${properties["jankson_version"]}")

	// Dev Util
	modLocalRuntime("maven.modrinth:jsst:B39piMwB")

	// COMPATIBILITY
	modImplementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

	// Recipe Viewer APIs
	// https://github.com/mezz/JustEnoughItems/issues/2891
	// modCompileOnlyApi("mezz.jei:jei-${properties["minecraft_version"]}-common-api:${properties["jei_version"]}")
	// modCompileOnlyApi("mezz.jei:jei-${properties["minecraft_version"]}-fabric-api:${properties["jei_version"]}")
	modCompileOnly("maven.modrinth:jei:${properties["jei_modrinth_id"]}")

	modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${properties["rei_version"]}")
	modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${properties["rei_version"]}")
	modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${properties["rei_version"]}")

	//modCompileOnly("dev.emi:emi-fabric:${properties["emi_version"]}:api")
	modCompileOnly("dev.emi:emi-fabric:${properties["emi_version"]}")

	// Recipe Viewer Runtimes
	//modRuntimeOnly("mezz.jei:jei-${properties["minecraft_version"]}-fabric:${properties["jei_version"]}")
	modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${properties["rei_version"]}") {
		exclude("net.fabricmc.fabric-api")
	}
	//modLocalRuntime("dev.emi:emi-fabric:${properties["emi_version"]}")
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		print("match")
		expand(inputs.properties)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

tasks.named<Jar>("sourcesJar") {
	dependsOn(tasks.classes)
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

val lastTag = properties["lastTag"]?.toString()
val newTag = properties["newTag"]?.toString()
if (lastTag != null && newTag != null) {
	val changelogPath = layout.buildDirectory.file("changelogs/$lastTag..$newTag.md")

	val changelogTask = task("generateChangelog") {
		val prefixList = properties["changelog_filter"]?.toString()?.split(",") ?: emptyList()
		println("Writing changelog to ${changelogPath.get()}")
		outputs.file(changelogPath)

		doLast {
			val command = "git log --max-count=100 --pretty=format:\"%s\" $lastTag...$newTag"
			val proc = Runtime.getRuntime().exec(command)
			// println(command)
			proc.errorStream.bufferedReader().forEachLine { println(it) }
			val lines = mutableListOf(
				// "# ${properties["mod_name"]} $newTag",
				"Previous: $lastTag",
				""
			)
			properties["github_url"]?.toString()?.also {
				lines.add("Full changelog: ${it}/compare/$lastTag...$newTag")
				lines.add("")
			}
			proc.inputStream.bufferedReader().forEachLine {
				var str = it
				// it starts with quotes in github actions i guess https://www.youtube.com/watch?v=-O3ogWBfWI0
				if (str.startsWith("\"")) str = str.substring(1)
				if (str.endsWith("\"")) str = str.substring(0, str.length - 1)
				if (prefixList.any { prefix -> str.startsWith(prefix) })
					lines.add("  - $str")
			}
			proc.waitFor()
			val changelog = lines.joinToString("\n")
			changelogPath.get().asFile.writeText(changelog)
		}
	}

	if (System.getenv().containsKey("GITHUB_TOKEN")) {
		tasks.named<GithubReleaseTask>("githubRelease") {
			dependsOn(changelogTask)
			mustRunAfter(changelogTask)
			inputs.file(changelogPath)

			authorization.set(System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" })
			owner.set(properties["github_owner"]!!.toString())
			repo.set(properties["github_repo"]!!.toString())
			tagName.set(newTag)
			releaseName.set("${properties["mod_name"]} $newTag")
			targetCommitish.set(grgit.branch.current().name)
			releaseAssets.from(
				tasks["remapJar"].outputs.files,
				tasks["remapSourcesJar"].outputs.files,
			)

			body.set(provider {
				return@provider changelogPath.get().asFile.readText()
			})
		}
	}

	tasks.named<DefaultTask>("publishMods") {
		dependsOn(changelogTask)
		mustRunAfter(changelogTask)
	}

	publishMods {
		changelog.set(provider {
			return@provider changelogPath.get().asFile.readText()
		})
		type.set(ReleaseType.STABLE)
		modLoaders.add("fabric")
		modLoaders.add("quilt")
		file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)
		additionalFiles.from(tasks.named<RemapSourcesJarTask>("remapSourcesJar").get().archiveFile)

		if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
			curseforge {
				projectId.set("378036")
				accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
				properties["game_versions"]!!.toString().split(",").forEach {
					minecraftVersions.add(it)
				}
				displayName.set("${properties["prefix"]!!} ${base.archivesName.get()}")
				listOf("fabric-api", "yacl").forEach { requires {
					slug.set(it)
				}}
				listOf("emi", "jei", "roughly-enough-items", "modmenu").forEach { optional {
					slug.set(it)
				}}
			}
		}

		if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
			modrinth {
				accessToken.set(System.getenv("MODRINTH_TOKEN"))
				projectId.set("FCTyEqkn")
				properties["game_versions"]!!.toString().split(",").forEach {
					minecraftVersions.add(it)
				}
				displayName.set("${properties["mod_name"]!!} $version")
				listOf("fabric-api", "yacl").forEach { requires {
					slug.set(it)
				}}
				listOf("emi", "jei", "rei", "modmenu").forEach { optional {
					slug.set(it)
				}}
			}
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"]!!)
		}
	}

	repositories {
		maven {
			name = "GitHubPackages"
			url = URI("https://maven.pkg.github.com/JackFred2/WhereIsIt")
			credentials {
				username = properties["gpr.user"]?.toString() ?: System.getenv("GPR_USER")
				password = properties["gpr.key"]?.toString() ?: System.getenv("GPR_TOKEN")
			}
		}
	}
}