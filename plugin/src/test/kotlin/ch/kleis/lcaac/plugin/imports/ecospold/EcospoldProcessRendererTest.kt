package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.ecospold.model.Classification
import ch.kleis.lcaac.plugin.imports.model.ImportedProcess
import ch.kleis.lcaac.plugin.imports.model.ImportedSubstance
import ch.kleis.lcaac.plugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaac.plugin.imports.shared.serializer.SubstanceSerializer
import io.mockk.*
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals


class EcospoldProcessRendererTest {
    private val writer = mockk<ModelWriter>()

    @Before
    fun before() {
    }

    @After
    fun after() {
        unmockkAll()
    }

    // FIXME
    fun render_shouldRender() {
        // Given
        justRun { writer.writeFile( any(), any()) }
        val activity = mockk<ActivityDataset>()
        every { activity.description.activity.name } returns "pName"
        every { activity.description.geography?.shortName } returns "ch"
        every { activity.description.classifications } returns listOf(Classification("EcoSpold01Categories", "cat"))
        mockkObject(EcoSpoldProcessMapper)
        val importedProcess = mockk<ImportedProcess>()
        every { EcoSpoldProcessMapper.map(activity, emptyMap(), emptySet()) } returns importedProcess
        val comments = mutableListOf<String>()
        every { importedProcess.comments } returns comments
        every { importedProcess.uid } returns "uid"
        mockkObject(ProcessSerializer)
        every { ProcessSerializer.serialize(importedProcess) } returns "serialized process"

        mockkObject(EcoSpoldSubstanceMapper)
        val importedSubstance = mockk<ImportedSubstance>()
        every { EcoSpoldSubstanceMapper.map(activity, "EF v3.1") } returns importedSubstance
        mockkObject(SubstanceSerializer)
        every { SubstanceSerializer.serialize(importedSubstance) } returns "serialized substance"
        val sut = EcoSpoldProcessRenderer()

        // When
        sut.render(
            data = activity,
            processDict = emptyMap(),
            knownUnits = emptySet(),
            writer = writer,
            processComment = "a comment",
            methodName = "EF v3.1"
        )

        // Then, Better way to view large diff than using mockk.verify
        verifyOrder {
            writer.writeFile("processes/cat/uid.lca", "serialized process")
            writer.writeFile("substances/cat/uid.lca", "serialized substance")
        }
        assertEquals(mutableListOf("a comment"), comments)
    }

}
