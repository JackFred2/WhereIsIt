import java.net.URI;
import org.gradle.api.publish.maven.MavenPublication

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.2-SNAPSHOT"
}

val version = findProperty("mod_version")
val group = findProperty("maven_group")

base {
	archivesName.set("${findProperty("archives_base_name")}-${findProperty("minecraft_version")}-$version")
}

repositories {
	repositories {
		maven {
			name = "ParchmentMC"
			url = URI("https://maven.parchmentmc.org")
			content {
				includeGroup("org.parchmentmc.data")
			}
		}
		maven {
			name = "TerraformersMC"
			url = URI("https://maven.terraformersmc.com/releases/")
			content {
				includeGroup("com.terraformersmc")
				includeGroup("dev.emi")
			}
		}
		maven {
			// JEI
			name = "Jared"
			url = URI("https://maven.blamejared.com/")
			content {
				includeGroup("mezz.jei")
			}
		}
		maven {
			name = "Shedaniel"
			url = URI("https://maven.shedaniel.me")
			content {
				includeGroupByRegex("me.shedaniel.*")
				includeGroup("dev.architectury")
			}
		}
		maven {
			name = "Xander Maven"
			url = URI("https://maven.isxander.dev/releases")
			content {
				includeGroup("dev.isxander.yacl")
			}
		}
		maven {
			name = "Modrinth"
			url = URI("https://api.modrinth.com/maven")
		}
	}
}

loom {
    splitEnvironmentSourceSets()

	mods {
		create("whereisit") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	accessWidenerPath.set(file("src/main/resources/whereisit.accesswidener"))
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${findProperty("minecraft_version")}")
	mappings(loom.layered() {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${findProperty("parchment_version")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${findProperty("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${findProperty("fabric_version")}")
	modImplementation("com.terraformersmc:modmenu:${findProperty("modmenu_version")}")

	// Config
	modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:${findProperty("yacl_version")}")
	implementation("blue.endless:jankson:${findProperty("jankson_version")}")

	// COMPATIBILITY

	// ItemStack Grabbers
	// https://github.com/mezz/JustEnoughItems/issues/2891
	// modCompileOnlyApi("mezz.jei:jei-${minecraft_version}-common-api:${jei_version}")
	// modCompileOnlyApi("mezz.jei:jei-${minecraft_version}-fabric-api:${jei_version}")
	modCompileOnlyApi("maven.modrinth:jei:${findProperty("jei_modrinth_id")}")

	// modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$rei_version")
	// modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$rei_version")
	modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${findProperty("rei_version")}")

	//modCompileOnly("dev.emi:emi-fabric:${emi_version}:api")
	modCompileOnly("dev.emi:emi-fabric:${findProperty("emi_version")}")

	//modRuntimeOnly("mezz.jei:jei-${minecraft_version}-fabric:${jei_version}")
	//modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${rei_version}")
	modLocalRuntime("dev.emi:emi-fabric:${findProperty("emi_version")}")
}

tasks.withType<ProcessResources>().configureEach {
	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${findProperty("archivesBaseName")}"}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "GitHubPackages"
			url = URI("https://maven.pkg.github.com/JackFred2/WhereIsIt")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
}