package task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

abstract class GenerateEmissionFactorsTask31 : DefaultTask() {
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "ch.kleis"
        description = "generateEmissionFactors3_1"
        inputDir.convention(this.project.layout.projectDirectory.dir("src/main/stdlib/ef3.1"))
        outputDir.convention(this.project.layout.buildDirectory.dir("stdlib/ef3.1"))
    }

    @TaskAction
    @Suppress("UNUSED_PARAMETER")
    fun execute(inputChanges: InputChanges) {
        GenerateEmissionFactorsTask<EF31Record>(inputDir, outputDir)
            .createLibArchive("31", "3.1", { i -> EF31Record(i) })

    }

}
