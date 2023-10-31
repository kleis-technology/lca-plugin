package ch.kleis.lcaac.plugin.actions.csv

import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.fixture.QuantityValueFixture
import ch.kleis.lcaac.plugin.fixture.UnitValueFixture
import io.mockk.mockk
import org.apache.commons.io.output.AppendableWriter
import org.junit.Test
import java.io.OutputStream
import kotlin.test.assertEquals


class CsvResultWriterTest {

    @Test
    fun write() {
        // given
        val request = CsvRequest(
            "p",
            emptyMap(),
            mapOf("comment" to 0, "id" to 1, "a" to 2, "b" to 3),
            listOf("""Comment, with comma, and "double quotes" """, "s00", "1.0", "2.0"),
        )
        val result = CsvResult(
            request,
            ProductValue("out", UnitValueFixture.kg()),
            mapOf(
                ProductValue<BasicNumber>("in1", UnitValueFixture.kg()) to QuantityValue(BasicOperations.pure(21234234923.3), UnitValueFixture.kg()),
                ProductValue<BasicNumber>("in2", UnitValueFixture.l()) to QuantityValueFixture.oneLitre,
            )
        )
        val outputStream = mockk<OutputStream>()
        val buffer = StringBuilder()
        val innerWriter = AppendableWriter(buffer)
        val writer = CsvResultWriter(
            outputStream,
            innerWriter,
        )

        // when
        writer.write(listOf(result))
        val actual = buffer.toString()

        // then
        val expected = """
            comment,id,a,b,product,reference unit,in1 [kg],in2 [l]
            "Comment, with comma, and ""double quotes"" ",s00,1.0,2.0,out,kg,2.12342349233E10,1.0
            
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
