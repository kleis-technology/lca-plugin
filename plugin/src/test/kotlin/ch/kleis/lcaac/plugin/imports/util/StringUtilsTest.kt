package ch.kleis.lcaac.plugin.imports.util

import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import ch.kleis.lcaac.plugin.imports.util.StringUtils.formatMetaValues
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compact
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compactList
import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize
import ch.kleis.lcaac.plugin.imports.util.StringUtils.trimTrailingNonPrinting
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StringUtilsTest {
    @Test
    fun compact_empty_isEmpty() {
        assertEquals("", compact(""))
    }

    @Test
    fun compact_removes_trailing_whitespace() {
        assertEquals("a string", compact("a string   "))
    }

    @Test
    fun compact_removes_trailing_newline() {
        assertEquals("a string", compact("a string\n"))
        assertEquals("a string", compact("a string\r\n"))
    }

    @Test
    fun compact_removes_double_quotes() {
        assertEquals("'a string'", compact("\"a string\""))
    }

    @Test
    fun compact_keeps_multiline() {
        val given = """
            hello
            world       
        """.trimIndent()
        val expected = """
            hello
            world""".trimIndent()
        assertEquals(expected, compact(given))
    }

    @Test
    fun test_compactList() {
        val given = listOf("identity", "trailing whitespace   ", "\"double \"quotes", "trailing newline\n")
        val `when` =  compactList(given)

        // then
        assertContentEquals(given.map(StringUtils::compact), `when`)
    }


    @Test
    fun trimTrailingNonPrinting_removes_trailing_whitespace() {
        assertEquals("a string", trimTrailingNonPrinting("a string   "))
    }

    @Test
    fun trimTrailingNonPrinting_removes_trailing_newline() {
        assertEquals("a string", trimTrailingNonPrinting("a string\n"))
        assertEquals("a string", trimTrailingNonPrinting("a string\r\n"))
    }

    @Test
    fun test_sanitize() {
        // Given
        val data = listOf(
            "01" to "_01",
            "ab" to "ab",
            "a_+__b" to "a_p_b",
            "a_*__b" to "a_m_b",
            "m*2a" to "m_m_2a",
            "a&b" to "a_a_b",
            "  a&b++" to "a_a_b_p_p",
        )

        data.forEach { (given, expected) ->
            assertEquals(expected, sanitize(given))
        }
    }

    @Test
    fun comment_respects_multiline() {
        val given = """
            hello world
            this will be commented
        """.trimIndent()

        val expected = """
            // hello world
            // this will be commented
        """.trimIndent()

        assertEquals(expected, asComment(given))
    }

    @Test
    fun blockKeyValue_prints_key_value() {
        val given = mapOf("key" to "value")
        val expected = "\"key\" = \"value\""

        assertEquals(expected, formatMetaValues(given).toString())
    }

    @Test
    fun blockKeyValue_prints_multiple_entries() {
        val given = mapOf("key" to "value", "other key" to "other value", "last key" to "last value")
        val expected = """"key" = "value"
                                |"other key" = "other value"
                                |"last key" = "last value"
                                """.trimMargin()
        assertEquals(expected, formatMetaValues(given).toString())
    }

    @Test
    fun blockKeyValue_prints_multiline_entries() {
        val given = mapOf("key" to "value", "other key" to "other value\none two three\n    you and me\n", "last key" to "last value")
        val expected = """"key" = "value"
                                |"other key" = "other value
                                |    one two three
                                |        you and me"
                                |"last key" = "last value"
                              """.trimMargin()
       assertEquals(expected, formatMetaValues(given).toString())
    }
}