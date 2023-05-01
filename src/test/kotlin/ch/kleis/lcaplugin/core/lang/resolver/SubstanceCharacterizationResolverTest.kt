package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNull


class SubstanceCharacterizationResolverTest {
    @Test
    fun resolve_whenEmptyTable_thenNull() {
        // given
        val symbolTable = SymbolTable.empty()
        val resolver = SubstanceCharacterizationResolver(symbolTable)
        val substance = SubstanceFixture.propanol

        // when
        val actual = resolver.resolve(substance)

        // then
        assertNull(actual)
    }

    @Test
    fun resolve_whenSingleExactMatch_shouldFindMatch() {
        // given
        val substance = SubstanceFixture.propanol
        val substanceCharacterization = SubstanceCharacterizationFixture.propanolCharacterization
        val substanceCharacterizations = Register.from(mapOf("propanol" to substanceCharacterization))

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(substance)

        // then
        assertEquals(substanceCharacterization, actual)
    }

    @Test
    fun resolve_whenTwoDifferentCompartments() {
        // given
        val propanolAir = SubstanceFixture.propanol.copy(compartment = "air")
        val propanolAirCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAir)

        val propanolWater = SubstanceFixture.propanol.copy(compartment = "water")
        val propanolWaterCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolWater)

        val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.from(
            mapOf(
                "a" to propanolAirCharacterization,
                "b" to propanolWaterCharacterization,
            )
        )

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(propanolAir)

        // then
        assertEquals(propanolAirCharacterization, actual)
    }

    @Test
    fun resolve_whenTwoDifferentSubCompartments() {
        // given
        val propanolAirSpaceG = SubstanceFixture.propanol.copy(compartment = "air", subcompartment = "airspace G")
        val propanolAirSpaceGCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAirSpaceG)

        val propanolAirSpaceE = SubstanceFixture.propanol.copy(compartment = "air", subcompartment = "airspace E")
        val propanolAirSpaceECharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAirSpaceE)

        val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.from(
            mapOf(
                "a" to propanolAirSpaceGCharacterization,
                "b" to propanolAirSpaceECharacterization,
            )
        )

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(propanolAirSpaceG)

        // then
        assertEquals(propanolAirSpaceGCharacterization, actual)
    }
}
