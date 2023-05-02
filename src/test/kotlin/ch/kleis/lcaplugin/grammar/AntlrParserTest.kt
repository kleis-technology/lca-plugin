package ch.kleis.lcaplugin.grammar

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import org.junit.Test
import kotlin.test.assertEquals

class AntlrParserTest {
    @Test
    fun test_antlrParser() {
        // given
        val content = """
            process p {
                products {
                    1 kg p
                }
            }
        """.trimIndent()
        val input = CharStreams.fromString(content)
        val lexer = LcaLangLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)

        // when
        val actual = parser.process()

        // then
        assertEquals(1, actual.block_products().size)
        val block = actual.block_products(0)
        val exchange = block.technoProductExchange()
        assertEquals("p", exchange.productRef().text)
        assertEquals("1kg", exchange.quantity().text)
    }
}
