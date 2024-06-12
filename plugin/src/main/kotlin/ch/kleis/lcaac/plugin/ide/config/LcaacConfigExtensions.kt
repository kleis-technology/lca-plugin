package ch.kleis.lcaac.plugin.ide.config

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import java.nio.file.Path

class LcaacConfigExtensions {
    fun Project.lcaacConfig(): LcaacConfig {
        val workingDirectory = this.basePath ?: throw IllegalStateException("Current project misses a base path")
        val candidates = listOf("lcaac.yaml", "lcaac.yml")
        return runReadAction {
            candidates.firstNotNullOfOrNull { filename ->
                val file = Path.of(workingDirectory, filename).toFile()
                if (file.exists()) file.inputStream().use {
                    Yaml.default.decodeFromStream(LcaacConfig.serializer(), it)
                }
                else null
            } ?: LcaacConfig(
                connectors = listOf(
                    ConnectorConfig(
                        "csv",
                        options = mapOf(
                            "directory" to workingDirectory
                        )
                    )
                )
            )
        }
    }
}
