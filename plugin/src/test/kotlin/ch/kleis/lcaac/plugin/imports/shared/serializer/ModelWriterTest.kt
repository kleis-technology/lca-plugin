package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelWriterTest {
    private val tmpDir = System.getProperty("java.io.tmpdir")

    @Before
    fun before() {
        Files.deleteIfExists(Paths.get(tmpDir, "relative_file.lca"))
        Files.deleteIfExists(Paths.get(tmpDir, "relative_file_1103262748.lca"))
    }

    @After
    fun after() {
        unmockkAll()
        Files.deleteIfExists(Paths.get(tmpDir, "relative_file.lca"))
        Files.deleteIfExists(Paths.get(tmpDir, "relative_file_1103262748.lca"))
    }

    @Test
    fun writeFile() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        justRun { watcher.notifyCurrentWork("relative_file") }
        val sut = ModelWriter("test", tmpDir, listOf("custom.import"), watcher)
        val expected = """package test
            |
            |import custom.import
            |Content
        """.trimMargin()


        // When
        sut.writeFile("relative_file", "Content")

        // Then
        verify { watcher.notifyCurrentWork("relative_file") }
        assertTrue(Paths.get(tmpDir, "relative_file.lca").toFile().exists())

        val fileReader = FileReader(Paths.get(tmpDir, "relative_file.lca").toFile())
        assertEquals(expected, fileReader.readText())
    }

    @Test
    fun writeAppendFile() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        justRun { watcher.notifyCurrentWork("relative_file") }
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)
        val expected = """package test
            |
            |import custom.import
            |Content
            |Another content
        """.trimMargin()

        // When
        sut.writeAppendFile("relative_file", "Content\n")
        sut.writeAppendFile("relative_file", "Another content")

        // Then
        verify(exactly = 2) { watcher.notifyCurrentWork("relative_file") }

        val file = Paths.get(tmpDir, "relative_file.lca").toFile()
        assertTrue(file.exists())
        assertEquals(expected, file.readText())

    }

    @Test
    fun writeRotateFile() {
        // given
        val watcher = mockk<AsynchronousWatcher>()
        justRun { watcher.notifyCurrentWork(any()) }
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)

        val expected1 = """package test
            |
            |import custom.import
            |Content
            |""".trimMargin()
        val expected2 = """package test
            |
            |import custom.import
            |Another Content
            """.trimMargin()

        // when
        sut.writeRotateFile("relative_file", "Content\n")
        sut.writeRotateFile("relative_file", "Another Content")

        // then
        val file1 = Paths.get(tmpDir, "relative_file.lca").toFile()
        assertTrue(file1.exists())
        assertEquals(expected1, file1.readText())

        val file2 = Paths.get(tmpDir, "relative_file_1103262748.lca").toFile()
        assertTrue(file2.exists())
        assertEquals(expected2, file2.readText())

    }
}