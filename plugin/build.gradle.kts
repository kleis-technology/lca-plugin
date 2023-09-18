import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import task.GenerateEmissionFactorsTask30
import task.GenerateEmissionFactorsTask31

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support, oom issue with this version see https://jb.gg/intellij-platform-kotlin-oom
    // TODO Move to next version to solve it when fix is available
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.14.2"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.0.0"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
    // Gradle Grammar kit Plugin
    id("org.jetbrains.grammarkit") version "2021.2.2"
    // Arrow optics auto-generation Plugin
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
    // JSON serialization tools for graph visualization
    kotlin("plugin.serialization") version "1.8.10"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

dependencies {
    implementation(project(":core"))
    testImplementation(project(":core"))

    implementation(files(layout.buildDirectory.dir("stdlib/ef3.1")) {
        builtBy("generateEmissionFactors31")
    })
    implementation(files(layout.buildDirectory.dir("stdlib/ef3.0")) {
        builtBy("generateEmissionFactors30")
    })


    implementation(platform("io.arrow-kt:arrow-stack:1.1.5"))
    implementation("io.arrow-kt:arrow-core")
    testImplementation("io.arrow-kt:arrow-optics")

    implementation("org.openlca:olca-simapro-csv:3.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jdom:jdom2:2.0.6.1")

    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation(kotlin("test-junit"))
}


// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    generateParser {
        source.set("src/main/kotlin/ch/kleis/lcaac/plugin/language/Lca.bnf")
        targetRoot.set("src/main/gen")
        pathToParser.set("ch/kleis/lcaac/plugin/language/parser/LcaParser.java")
        pathToPsiRoot.set("ch/kleis/lcaac/plugin/psi")
    }

    generateLexer {
        source.set("src/main/kotlin/ch/kleis/lcaac/plugin/language/Lca.flex")
        targetDir.set("src/main/gen/ch/kleis/lcaac/plugin/language")
        targetClass.set("parser.LcaLexer")
    }

    task<GenerateEmissionFactorsTask31>("generateEmissionFactors31") {
    }
    task<GenerateEmissionFactorsTask30>("generateEmissionFactors30") {
    }

    compileKotlin {
        dependsOn("generateLexer")
        dependsOn("generateParser")
        dependsOn("generateEmissionFactors30")
        dependsOn("generateEmissionFactors31")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.renderItem(changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }, Changelog.OutputType.HTML)
        })
    }

    runIde {
        systemProperty("jdk.attach.allowAttachSelf", "true")
        maxHeapSize = "4G"
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(File("./local/credentials/chain.crt").readText(Charsets.UTF_8))
        privateKey.set(File("./local/credentials/private.pem").readText(Charsets.UTF_8))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }

    clean {
        delete("${rootDir}/src/main/gen")
    }
}

afterEvaluate {
    tasks.findByName("kspKotlin")?.mustRunAfter(tasks.generateLexer, tasks.generateParser)
}