package ch.kleis.lcaac.plugin.imports

import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists


private const val MAX_FILE_SIZE = 2000000

data class FileWriterWithSize(val writer: FileWriter, val currentIndex: Int, var currentSize: Int = 0) :
    Closeable {

    constructor(path: Path, currentSize: Int) :
            this(FileWriter(Files.createFile(path).toFile(), Charset.forName("UTF-8")), currentSize)


    fun write(block: CharSequence) {
        val str = "$block\n"
        writer.write(str)
        currentSize += str.length
    }

    fun isFull(): Boolean {
        return currentSize > MAX_FILE_SIZE
    }

    override fun close() {
        writer.close()
    }
}

class ModelWriter(
    private val packageName: String,
    private val rootFolder: String,
    private val imports: List<String> = listOf(),
    private val watcher: AsynchronousWatcher
) : Closeable {
    companion object {
        private val LOG = Logger.getInstance(ModelWriter::class.java)
    }

    private val openedFiles: MutableMap<String, FileWriterWithSize> = mutableMapOf()

    private fun FileWriter.writeHeaders() {
        this.write("package $packageName\n\n")
        imports.forEach { this.write("import $it\n") }
    }

    fun writeFile(relativePath: String, block: String) {
        if (block.isNotBlank()) {
            watcher.notifyCurrentWork(relativePath)

            val path = buildPathOfRelativePath(relativePath)
            path.deleteIfExists()
            Files.createDirectories(path.parent)

            utf8FileWriter(Files.createFile(path).toFile()).use {
                it.writeHeaders()
                it.write(block)
            }
        }
    }

    @Suppress("unused")
    fun writeAppendFile(relativePath: String, block: String) {
        if (block.isNotBlank()) {
            watcher.notifyCurrentWork(relativePath)

            val path = buildPathOfRelativePath(relativePath)
            if (path.exists()) {
                utf8FileWriter(path.toFile()).use {
                    it.write(block)
                }
            } else {
                writeFile(relativePath, block)
            }
        }
    }

    private fun utf8FileWriter(file: File): FileWriter = FileWriter(file, Charset.forName("UTF-8"))

    private fun buildPathOfRelativePath(relativePath: String): Path =
        Paths.get(rootFolder, File.separatorChar.toString(), relativePath, ".lca")

    fun write(relativePath: String, block: CharSequence, index: Boolean = true, closeAfterWrite: Boolean = false) {
        if (block.isNotBlank()) {
            watcher.notifyCurrentWork(relativePath)
            val file = recreateIfNeeded(relativePath, index)
            file.write(block)
            if (closeAfterWrite) {
                openedFiles.remove(relativePath)?.close()
            }
        }
    }

    private fun recreateIfNeeded(relativePath: String, index: Boolean): FileWriterWithSize {
        val existingFile = openedFiles[relativePath]
        return if (existingFile == null) {
            createNewFile(relativePath, 1, index)
        } else if (existingFile.isFull() && index) {
            existingFile.close()
            @Suppress("KotlinConstantConditions")
            createNewFile(relativePath, existingFile.currentIndex + 1, index)
        } else {
            existingFile
        }
    }

    private fun createNewFile(relativePath: String, currentIndex: Int, index: Boolean): FileWriterWithSize {
        val extension = if (index) "_$currentIndex.lca" else ".lca"
        val path = Paths.get(rootFolder + File.separatorChar + relativePath + extension)
        if (path.exists()) path.deleteExisting()
        Files.createDirectories(path.parent)
        val new = FileWriterWithSize(path, currentIndex)
        openedFiles[relativePath] = new
        new.write("package $packageName\n")
        imports.forEach { new.write("import $it") }
        return new
    }

    override fun close() {
        openedFiles.entries.forEach { (path, writer) ->
            try {
                writer.close()
                val fullPath = Paths.get(rootFolder + File.separatorChar + path)
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(fullPath.toString())
                virtualFile?.let {
                    RefreshQueue.getInstance()
                        .refresh(false, false, null, ModalityState.current(), it)
                }
            } catch (e: IOException) {
                LOG.error("Unable to close file $path", e)
            }
        }
    }
}
