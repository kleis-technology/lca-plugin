package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.shared.serializer.UnitSerializer
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

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
    fun render_whenNewUnit_shouldWrite() {
        // given
        val manager = UnitManager()
        val importedUnit = ImportedUnit("dimension", "symbol")
        val serializer = mockk<UnitSerializer>()
        every { serializer.serialize(any()) } returns "this is the body"
        val renderer = UnitRenderer(manager, serializer)

        // when
        renderer.render(importedUnit, writer)

        // then
        assertEquals("unit", pathSlot.captured)
        assertEquals("this is the body", bodySlot.captured)
    }

    @Test
    fun render_whenKnownUnit_shouldNotWrite() {
        // given
        val manager = UnitManager()
        val importedUnit = ImportedUnit("dimension", "symbol")
        manager.add(importedUnit)
        val serializer = mockk<UnitSerializer>()
        every { serializer.serialize(any()) } returns "this is the body"
        val renderer = UnitRenderer(manager, serializer)

        // when
        renderer.render(importedUnit, writer)

        // then
        verify(exactly = 0) { writer.writeAppendFile(any(), any()) }
    }
}
