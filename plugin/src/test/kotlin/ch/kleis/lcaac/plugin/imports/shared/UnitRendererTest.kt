package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.plugin.imports.ModelWriter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.fail

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
    fun testMe() {
        fail("test me")
    }
}
