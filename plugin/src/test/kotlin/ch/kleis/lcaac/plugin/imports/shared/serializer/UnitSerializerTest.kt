package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.model.ImportedUnitAliasFor
import org.junit.Test
import kotlin.test.assertEquals

class UnitSerializerTest {

    @Test
    fun serialize_whenLiteral() {
        // given
        val unit = ImportedUnit("unit dim", "a + b*c")
        val serializer = UnitSerializer()

        // when
        val actual = serializer.serialize(unit)

        // then
        val expected = """
            unit a_p_b_m_c {
                symbol = "a + b*c"
                dimension = "unit dim"
            }
            
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun serialize_withAliasFor() {
        // given
        val unit = ImportedUnit(
            "unit dim", "a + b*c",
            ImportedUnitAliasFor(2.0, "some * expression")
        )
        val serializer = UnitSerializer()

        // when
        val actual = serializer.serialize(unit)

        // then
        val expected = """
            unit a_p_b_m_c {
                symbol = "a + b*c"
                alias_for = 2.0 some * expression
            }
            
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
