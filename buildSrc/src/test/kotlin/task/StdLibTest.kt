package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


class StdLibTest {
    @Test
    fun sanitizeString_whenNormal_thenDoNothing() {
        // given
        val s = "abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "abcd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpace_shouldReplaceWithUnderscore() {
        // given
        val s = "ab   cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpecialSymbols_shouldRemove() {
        // given
        val s = "ab(#+cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenBeginsWithNumber_shouldPrependWithUnderscore() {
        // given
        val s = "123abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "_123abcd"
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "w123a>bcd, w123a_gt_bcd, Greater than conversion",
        "w123a<bcd, w123a_lt_bcd, Lesser than conversion",
        "w123a/bcd, w123a_sl_bcd, Slash conversion",
    )
    fun sanitizeString_withSpecialChar_shouldBeReplace(source: String, expected: String, desc: String) {
        // when
        val actual = sanitizeString(source)

        // then
        assertEquals(expected, actual, desc)
    }

    @Test
    fun generateZipEntry_withNestedEntry_shouldProduceArchiveReadableSequentially(@TempDir tempDir: File) {
        // given
        val archive = File(tempDir, "test.jar")

        // when
        ZipOutputStream(archive.outputStream()).use {
            generateZipEntry(it, "substance", "content")
            generateZipEntry(it, "META-INF/dictionary.csv", "Name;Type")
        }

        // then
        // JetBrains Marketplace reads plugin JARs sequentially, so every local entry
        // must be readable and match the central directory
        val sequentialEntries = ZipInputStream(archive.inputStream()).use { zip ->
            generateSequence { zip.nextEntry }
                .map { entry -> entry.name to zip.readBytes().decodeToString() }
                .toList()
        }
        val centralDirectoryEntries = ZipFile(archive).use { zip -> zip.entries().toList().map { it.name } }
        val expected = listOf("substance.lca" to "content", "META-INF/dictionary.csv" to "Name;Type")
        assertEquals(expected, sequentialEntries)
        assertEquals(expected.map { it.first }, centralDirectoryEntries)
    }
}
