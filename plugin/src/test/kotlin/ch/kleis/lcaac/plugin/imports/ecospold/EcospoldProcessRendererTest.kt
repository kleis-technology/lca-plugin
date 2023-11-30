package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals


class EcospoldProcessRendererTest {
    @Test
    fun test_shouldRender() {
        // given
        val data = EcoSpold2Fixture.buildData()
        val dict = EcoSpold2Fixture.buildProcessDict()
        val writer = mockk<ModelWriter>()
        val blockSlot = slot<CharSequence>()
        every { writer.writeRotateFile(any(), capture(blockSlot)) } returns Unit

        val processComment = ""
        val methodName = "EF v3.1"

        val unitManager = UnitManager()
        unitManager.add(ImportedUnit("climate_change", "kg CO2-Eq"))
        unitManager.add(ImportedUnit("acidification", "mol H+-Eq"))
        val renderer = EcoSpoldProcessRenderer(unitManager, dict, writer, methodName)

        // when
        renderer.render(data, processComment)

        // then
        verify {
            writer.writeRotateFile(any(), any())
        }
        val expected = """
            |process aname_ch {
            |
            |    meta {
            |        "id" = "aId"
            |        "name" = "aName"
            |        "type" = "1"
            |        "description" = "ageneralComment"
            |        "energyValues" = "123"
            |        "includedActivitiesStart" = "includedActivitiesStart"
            |        "includedActivitiesEnd" = "includedActivitiesEnd"
            |        "geography-shortname" = "ch"
            |        "geography-comment" = "comment"
            |        "System" = "Value"
            |    }
            |
            |    labels {
            |        productName = "pname"
            |    }
            |
            |    products {
            |        // pName
            |        // PSystem = PValue
            |        // // uncertainty: logNormal mean=1.2, variance=2.3, mu=3.4
            |        // synonym_0 = p1
            |        1.0 km pname allocate 100.0 percent
            |    }
            |
            |    inputs {
            |        3.0 kg iname from iname_producing_process_glo match (productName = "iname")
            |        25.0 m3 iname2 from iname2_producing_process_ch match (productName = "iname2")
            |    }
            |
            |    emissions {
            |        1.8326477008541038E-8 kg _1_2_dichlorobenzene(compartment = "air", sub_compartment = "urban air close to ground")
            |    }
            |
            |    resources {
            |        0.004413253823373581 kg nitrogen(compartment = "natural resource", sub_compartment = "land")
            |    }
            |
            |    land_use {
            |        0.04997982922431679 m2*year occupation_annual_crop_irrigated(compartment = "natural resource", sub_compartment = "land")
            |    }
            |
            |    impacts { // Impacts for method EF v3.1
            |        // accumulated exceedance (AE)
            |        0.0013 mol_H_p_Eq acidification
            |        // global warming potential (GWP100)
            |        0.6 kg_CO2_Eq climate_change
            |    }
            |
            |}
            |
        """.trimMargin()
        assertEquals(expected, blockSlot.captured.toString())
    }
}
