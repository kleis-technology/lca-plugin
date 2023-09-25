package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.TestUtils
import ch.kleis.lcaac.plugin.imports.FileWriterWithSize
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import com.intellij.openapi.vfs.LocalFileSystem
import io.mockk.*
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class ModelWriterTest {

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun write() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        justRun { watcher.notifyCurrentWork("relative_file") }
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)


        // When
        sut.write("relative_file", "Content", false)

        // Then
        verify { watcher.notifyCurrentWork("relative_file") }
        assertTrue(File(System.getProperty("java.io.tmpdir") + File.separator + "relative_file.lca").exists())
    }

    @Test
    fun close_ShouldCloseSubResources() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)
        val existingWriter = mockk<FileWriterWithSize>()
        justRun { existingWriter.close() }
        val opened = mutableMapOf("relative_file.lca" to existingWriter)
        TestUtils.setField(sut, "openedFiles", opened)
        mockkStatic(LocalFileSystem::class)
        val fileSys = mockk<LocalFileSystem>()
        every { LocalFileSystem.getInstance() } returns fileSys
        every { fileSys.findFileByPath(any()) } returns null

        // When
        sut.close()

        // Then
        verify { existingWriter.close() }
    }
}