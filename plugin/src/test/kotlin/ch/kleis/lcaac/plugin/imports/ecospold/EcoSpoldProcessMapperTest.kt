package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.prelude.Prelude.Companion.sanitize
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.model.ImportedImpactExchange
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import ch.kleis.lcaac.plugin.imports.util.ImportException
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol
import com.intellij.testFramework.UsefulTestCase.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EcoSpoldProcessMapperTest {

    private val sub: ActivityDataset = EcoSpold2Fixture.buildData()
    private val processDict: Map<String, EcoSpoldImporter.ProcessDictRecord> = EcoSpold2Fixture.buildProcessDict()

    @Test
    fun map_shouldMapKnownUnits() {
        // When
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager, "EF v3.1")
        unitManager.add(ImportedUnit("GWP", "kg CO2-Eq"))

        val importedProcess = mapper.map(sub)
        val actual = importedProcess.impactBlocks.toList().first()
            .exchanges.toList()[1]
            .unit

        // Then
        val expected = "kg_CO2_Eq"
        assertEquals(expected, actual)
    }

    @Test
    fun map_ShouldMapMeta() {
        // Given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // When
        val result = mapper.map(sub)

        // Then
        assertEquals("aname_ch", result.uid)
        assertEquals("aName", result.meta["name"])
        assertEquals("ageneralComment", result.meta["description"])
        assertEquals("123", result.meta["energyValues"])
        assertEquals("includedActivitiesStart", result.meta["includedActivitiesStart"])
        assertEquals("includedActivitiesEnd", result.meta["includedActivitiesEnd"])
        assertEquals("Value", result.meta["System"])
        assertEquals("ch", result.meta["geography-shortname"])
        assertEquals("comment", result.meta["geography-comment"])
    }

    @Test
    fun map_shouldMapEmissions() {
        // given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // when
        val result = mapper.map(sub)

        // then
        assertEquals(1, result.emissionBlocks.size)
        assertEquals(1, result.emissionBlocks[0].exchanges.count())
        val e = result.emissionBlocks[0].exchanges.first()
        assertEquals("1.8326477008541038E-8", e.qty)
        assertEquals("_1_2_dichlorobenzene", e.name)
        assertEquals("kg", e.unit)
        assertEquals("air", e.compartment)
        assertEquals("urban air close to ground", e.subCompartment)
        assertEquals(listOf(), e.comments)
    }

    @Test
    fun map_shouldMapLandUse() {
        // given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // when
        val result = mapper.map(sub)

        // then
        assertEquals(1, result.landUseBlocks.size)
        assertEquals(1, result.landUseBlocks[0].exchanges.count())
        val lu = result.landUseBlocks[0].exchanges.first()
        assertEquals("0.04997982922431679", lu.qty)
        assertEquals("occupation_annual_crop_irrigated", lu.name)
        assertEquals("m2*year", lu.unit)
        assertEquals("natural resource", lu.compartment)
        assertEquals("land", lu.subCompartment)
        assertEquals(listOf(), lu.comments)
    }

    @Test
    fun map_shouldMapResource() {
        // given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // when
        val result = mapper.map(sub)

        // then
        assertEquals(1, result.resourceBlocks.size)
        assertEquals(1, result.resourceBlocks[0].exchanges.count())
        val res = result.resourceBlocks[0].exchanges.first()
        assertEquals("0.004413253823373581", res.qty)
        assertEquals("nitrogen", res.name)
        assertEquals("kg", res.unit)
        assertEquals("natural resource", res.compartment)
        assertEquals("land", res.subCompartment)
        assertEquals(listOf(), res.comments)
    }

    @Test
    fun map_ShouldMapProduct() {
        // Given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // When
        val result = mapper.map(sub)

        // Then
        assertEquals(1, result.productBlocks.size)
        assertEquals(1, result.productBlocks[0].exchanges.count())
        val p = result.productBlocks[0].exchanges.first()
        assertEquals("pname", p.name)
        assertEquals("1.0", p.qty)
        assertEquals("km", p.unit)
        assertEquals(100.0, p.allocation)
        assertEquals(
            listOf(
                "pName",
                "PSystem = PValue",
                "// uncertainty: logNormal mean=1.2, variance=2.3, mu=3.4",
                "synonym_0 = p1"
            ), p.comments
        )

    }

    @Test
    fun map_ShouldMapInputs() {
        // given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)

        // when
        val result = mapper.map(sub)

        // then
        assertEquals(1, result.inputBlocks.size)
        assertEquals(2, result.inputBlocks[0].exchanges.count())

        val i1 = result.inputBlocks[0].exchanges.first()
        assertEquals("iname", i1.name)
        assertEquals("3.0", i1.qty)
        assertEquals("kg", i1.unit)
        assertEquals("iname_producing_process_glo match (productName = \"iname\")", i1.fromProcess)

        val i2 = result.inputBlocks[0].exchanges.elementAt(1)
        assertEquals("iname2", i2.name)
        assertEquals("25.0", i2.qty)
        assertEquals("m3", i2.unit)
        assertEquals("iname2_producing_process_ch match (productName = \"iname2\")", i2.fromProcess)
    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidInput() {
        // Given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)
        val falseSub = EcoSpold2Fixture.buildData(inputGroup = 4)

        // When
        val e = assertFailsWith(
            ImportException::class,
        ) {
            mapper.map(falseSub).productBlocks[0].exchanges.count()
        }
        assertEquals("Invalid inputGroup for intermediateExchange, expected in {1, 2, 3, 5}, found 4", e.message)
    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidProduct() {
        // Given
        val unitManager = UnitManager()
        val mapper = EcoSpoldProcessMapper(processDict, unitManager)
        val falseSub = EcoSpold2Fixture.buildData(1)
        assertEquals(1, falseSub.flowData.intermediateExchanges.first().outputGroup)

        // When
        assertThrows(
            ImportException::class.java,
            "Invalid outputGroup for product, expected 0, found 1"
        ) {
            mapper.map(falseSub).productBlocks[0].exchanges.count()
        }
    }

    @Test
    fun map_shouldMapImpacts() {
        // Given
        val unitManager = UnitManager()
        unitManager.add(ImportedUnit("acidification", "mol H+-Eq"))
        val mapper = EcoSpoldProcessMapper(processDict, unitManager, "EF v3.1")

        // When
        val result = mapper.map(sub)

        // Then
        assertEquals(1, result.impactBlocks.size)
        assertEquals(2, result.impactBlocks[0].exchanges.count())

        // First impact in fixture - not included, wrong method
        assertNotEquals(
            ImportedImpactExchange(
                qty = "0.1188",
                unit = "m3_world_eq_deprived",
                name = "deprivation",
                comments = listOf("water use"),
            ),
            result.impactBlocks[0].exchanges.first()
        )

        // Second impact in fixture - included, good method
        assertEquals(
            ImportedImpactExchange(
                qty = "0.0013",
                unit = "mol_H_p_Eq",
                name = "acidification",
                comments = listOf("accumulated exceedance (AE)"),
            ),
            result.impactBlocks[0].exchanges.first()
        )
    }

    @Test
    fun test_unitToStr_should_rewrite_imported_unit() {
        // given
        val unitManager = UnitManager()
        unitManager.add(ImportedUnit("acidification", "mol H+-Eq"))
        val unitName = "mol H+-Eq"
        val sanitizedUnitName = sanitize(sanitizeSymbol(unitName), toLowerCase = false)

        // when
        val result = unitManager.findRefBySymbolOrSanitizeSymbol(unitName)

        // then
        assertEquals(sanitizedUnitName, result)
    }

    @Test
    fun test_unitToStr_should_not_rewrite_unknown_unit() {
        // given
        val unitManager = UnitManager()
        val unitName = "an unknown unit"

        // when
        val result = unitManager.findRefBySymbolOrSanitizeSymbol(unitName)

        // then
        assertEquals(unitName, result)
    }

}
