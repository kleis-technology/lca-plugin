package ch.kleis.lcaac.plugin.imports

import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.math.abs


class ModelWriter(
    private val packageName: String,
    private val rootFolder: String,
    private val imports: List<String> = listOf(),
    private val watcher: AsynchronousWatcher
) {
    companion object {
        private val LOG = Logger.getInstance(ModelWriter::class.java)
    }

    private fun FileWriter.writeHeaders() {
        this.write("package $packageName\n\n")
        imports.forEach { this.write("import $it\n") }
    }

    fun writeFile(relativePath: String, block: CharSequence) {
        LOG.debug("writing to file $relativePath")
        if (block.isNotBlank()) {
            watcher.notifyCurrentWork(relativePath)

            val path = buildPathOfRelativePath(relativePath)
            path.deleteIfExists()
            Files.createDirectories(path.parent)

            utf8FileWriter(Files.createFile(path).toFile()).use {
                it.writeHeaders()
                it.write(block.toString())
            }
        }
    }

    fun writeAppendFile(relativePath: String, block: CharSequence) {
        LOG.debug("write-appending to file $relativePath")
        if (block.isNotBlank()) {
            val path = buildPathOfRelativePath(relativePath)
            if (path.exists()) {
                watcher.notifyCurrentWork(relativePath)
                utf8FileAppendWriter(path.toFile()).use {
                    it.write(block.toString())
                }
            } else {
                writeFile(relativePath, block)
            }
        }
    }

    fun writeRotateFile(relativePath: String, block: CharSequence) {
        LOG.debug("write-rotating to file $relativePath")
        if (block.isNotBlank()) {
            val path = buildPathOfRelativePath(relativePath)
            if (path.exists()) {
                val hash = abs(block.hashCode())
                val newRelativePath = "${relativePath}_$hash"
                writeFile(newRelativePath, block)
            } else {
                writeFile(relativePath, block)
            }
        }
    }

    private fun utf8FileWriter(file: File): FileWriter = FileWriter(file, Charset.forName("UTF-8"))
    private fun utf8FileAppendWriter(file: File): FileWriter = FileWriter(file, Charset.forName("UTF-8"), true)

    private fun buildPathOfRelativePath(relativePath: String): Path =
        Paths.get(rootFolder, "$relativePath.lca")
}
