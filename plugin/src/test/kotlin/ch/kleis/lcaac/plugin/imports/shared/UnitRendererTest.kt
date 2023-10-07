package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.util.ImportException
import io.mockk.*
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class UnitRendererTest {

    private val writer = mockk<ModelWriter>()
    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()

    @Before
    fun before() {
        every { writer.writeAppendFile(capture(pathSlot), capture(bodySlot)) } returns Unit
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun fail() {
        TODO("Fix tests below")
    }

    /*
    @Test
    fun test_writeUnit_ShouldReturnWithoutWritingWhenAlreadyExistWithCompatibleDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf("kg" to UnitValue(UnitSymbol.of("k+g"), 1.0, Prelude.mass)))
        val data = ImportedUnit("Mass", "k+g", 1.0, "kg")

        // When
        sut.render(data, writer)
        // Then
        verify(exactly = 0) { writer.writeAppendFile(any(), any()) }
    }

    @Test
    fun test_writeUnit_ShouldDeclareUnitWhenItsTheReferenceForNewDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue(UnitSymbol.of("k+g"), 1.0, Prelude.mass))))
        val data = ImportedUnit("Time", "s€C", 1.0, "s")

        // When
        sut.render(data, writer)

        // Then
        val expected = """

            unit s_C {
                symbol = "s€C"
                dimension = "time"
            }
            """.trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
    }

    @Test
    fun render_ShouldDeclareUnitWithComment_WhenItsTheReferenceForNewDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue(UnitSymbol.of("k+g"), 1.0, Prelude.mass))))
        val data = ImportedUnit("Time", "s€c", 1.0, "s", "Test")

        // When
        sut.render(data, writer)

        // Then
        // Better way to view large diff than using mockk.verify
        assertThat(bodySlot.captured, containsString("unit s_c { // Test"))
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWhenItsAnAliasForExistingDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("m2", UnitValue(UnitSymbol.of("m2"), 1.0, Prelude.length.pow(2.0)))))
        val data = ImportedUnit("Area", "me2", 1.0, "m2")

        // When
        sut.render(data, writer)

        // Then
        val expected = """

            unit me2 {
                symbol = "me2"
                alias_for = 1.0 m2
            }
            """.trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
    }

    @Test
    fun render_ShouldWriteComment_WhenDeclareAliasWhenItsAnAliasForExistingDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("m2", UnitValue(UnitSymbol.of("m2"), 1.0, Prelude.length.pow(2.0)))))
        val data = ImportedUnit("Area", "me2", 1.0, "m2", "Test")

        // When
        sut.render(data, writer)

        // Then
        // Better way to view large diff than using mockk.verify
        assertThat(bodySlot.captured, containsString("unit me2 { // Test"))
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWithTheRightCase() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("MJ", UnitValue(UnitSymbol.of("MJ"), 1.0, Prelude.length.pow(2.0)))))
        val data = ImportedUnit("Energy", "GJ", 1000.0, "mj")

        // When
        sut.render(data, writer)

        // Then
        val expected = """

            unit GJ {
                symbol = "GJ"
                alias_for = 1000.0 mj
            }
            """.trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWhenItsNotTheReference() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("s", UnitValue(UnitSymbol.of("S"), 1.0, Prelude.mass))))
        val data = ImportedUnit("Time", "s€c", 2.0, "s")

        // When
        sut.render(data, writer)

        // Then
        val expected = """

            unit s_c {
                symbol = "s€c"
                alias_for = 2.0 s
            }
""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
    }

    @Test
    fun test_writeUnit_ShouldRecordNewUnit() {
        // Given
        val sut = UnitRenderer.of(emptyMap())
        val data = ImportedUnit("Time", "s€c", 2.0, "s")

        // When
        sut.render(data, writer)

        // Then
        verify(atMost = 1) {
            writer.writeAppendFile("unit", any())
        }
    }

    @Test
    fun test_writeUnit_ShouldFailWithAnotherDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf("kg" to UnitValue(UnitSymbol.of("k+g"), 1.0, Prelude.mass)))
        val data = ImportedUnit("Time", "k+g", 1.0, "kg")
        val message = "A Unit kg for k+g already exists with another dimension, time is not compatible with mass."

        // When + Then
        val e = assertFailsWith(ImportException::class, null) { sut.render(data, writer) }
        assertEquals(message, e.message)
    }

    @Test
    fun test_writeUnit_ShouldFailWithAReferenceToItselfInAnExistingDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("g", UnitValue(UnitSymbol.of("g"), 1.0, Prelude.mass))))
        val data = ImportedUnit("mass", "kg", 1.0, "kg")
        val message = "Unit kg is referencing itself in its own declaration"

        // When + Then
        val e = assertFailsWith(ImportException::class, null) { sut.render(data, writer) }
        assertEquals(message, e.message)
    }

    @Test
    fun test_areCompatible() {
        // Given
        val input = listOf(
            Triple(Prelude.length, Prelude.length, true),
            Triple(Dimension.of("volume"), Prelude.length.pow(3.0), true),
            Triple(Prelude.length.pow(3.0), Dimension.of("volume"), true),

            Triple(Dimension.of("power"), Prelude.energy.divide(Prelude.time), true),
            Triple(Prelude.energy.divide(Prelude.time), Dimension.of("power"), true),

            Triple(Dimension.of("volume"), Prelude.length.pow(3.0), true),
            Triple(Prelude.length.pow(3.0), Dimension.of("volume"), true),

            Triple(Prelude.none, Dimension.of("amount"), true),
            Triple(Dimension.of("amount"), Prelude.none, true),

            Triple(Prelude.transport, Dimension.of("transport"), true),
            Triple(Dimension.of("transport"), Prelude.transport, true),

            Triple(Prelude.length_time, Dimension.of("length.time"), true),
            Triple(Dimension.of("length.time"), Prelude.length_time, true),

            Triple(Prelude.person_distance, Dimension.of("person.distance"), true),
            Triple(Dimension.of("person.distance"), Prelude.person_distance, true),

            Triple(Prelude.volume_time, Dimension.of("volume.time"), true),
            Triple(Dimension.of("volume.time"), Prelude.volume_time, true),

            Triple(Prelude.radioactivity, Dimension.of("amount"), false),
            Triple(Dimension.of("amount"), Prelude.radioactivity, false),
        )
        val sut = UnitRenderer.of(emptyMap())

        // When + Then
        input.forEach {
            assertEquals(
                "Invalid result for ${it.first} and ${it.second}, expected ${it.third}",
                it.third,
                sut.areCompatible(it.first, it.second)
            )
        }
    }


     */
}
