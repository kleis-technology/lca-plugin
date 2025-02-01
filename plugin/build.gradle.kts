
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import task.GenerateEmissionFactorsTask30
import task.GenerateEmissionFactorsTask31

fun properties(key: String) = project.findProperty(key).toString()

val group = properties("lcaacGroup")
val pluginVersion = properties("lcaacVersion")
val javaVersion = properties("javaVersion")

plugins {
    id("java")
    id("org.jetbrains.changelog")
    id("org.jetbrains.grammarkit")
    id("org.jetbrains.intellij")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.qodana")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(Integer.parseInt(javaVersion))
}

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        name = "github"
        url = uri("https://maven.pkg.github.com/kleis-technology/lcaac")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

dependencies {
    implementation("ch.kleis.lcaac:core:1.7.11")

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
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    val kotestVersion = "5.7.2"
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    implementation("com.charleskorn.kaml:kaml:0.59.0")
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
    version.set(pluginVersion)
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

    task<GenerateEmissionFactorsTask31>("generateEmissionFactors31")
    task<GenerateEmissionFactorsTask30>("generateEmissionFactors30")

    compileKotlin {
        dependsOn("generateLexer")
        dependsOn("generateParser")
        dependsOn("generateEmissionFactors30")
        dependsOn("generateEmissionFactors31")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
    }

    patchPluginXml {
        version.set(pluginVersion)
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
                getOrNull(pluginVersion) ?: getLatest()
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
        certificateChain.set(File("./.local/credentials/chain.crt").readText(Charsets.UTF_8))
        privateKey.set(File("./.local/credentials/private.pem").readText(Charsets.UTF_8))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        val channel = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
        channels.set(channel)
    }

    clean {
        delete("${rootDir}/src/main/gen")
    }
}
