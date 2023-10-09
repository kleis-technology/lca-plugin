package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class UnitManagerTest {
    @Test
    fun add_whenEmpty_shouldAdd() {
        // given
        val importedUnit = ImportedUnit("dimension", "symbol")
        val manager = UnitManager()

        // when
        manager.add(importedUnit)

        // then
        assertEquals(1, manager.numberOfAddInvocations)
        assertEquals(importedUnit.ref(), manager.findRefBySymbolOrSanitizeSymbol("symbol"))
    }

    @Test
    fun findRefBySymbol_whenPrelude_shouldFindRef() {
        // given
        val manager = UnitManager()

        // then
        Prelude.unitMap<BasicNumber>().forEach { (ref, unitLiteral) ->
            val symbol = unitLiteral.symbol.toString()
            val actual = manager.findRefBySymbolOrSanitizeSymbol(symbol)
            assertEquals(ref, actual)
        }
    }

    @Test
    fun findRefBySymbol_whenMetricTonKmAndNotAddedInManager_shouldSanitize() {
        // given
        val importedUnit = ImportedUnit("dimension", "metric ton*km")
        val manager = UnitManager()

        // when
        val actual = manager.findRefBySymbolOrSanitizeSymbol(importedUnit.symbol)

        // then
        assertEquals("ton*km", actual)
    }

    @Test
    fun findRefBySymbol_whenUnitAndNotAddedInManager_shouldSanitize() {
        // given
        val importedUnit = ImportedUnit("dimension", "unit")
        val manager = UnitManager()

        // when
        val actual = manager.findRefBySymbolOrSanitizeSymbol(importedUnit.symbol)

        // then
        assertEquals("u", actual)
    }

    @Test
    fun findRefBySymbol_whenReservedKeywordAndNotAddedInManager_shouldSanitize() {
        // given
        val importedUnit = ImportedUnit("dimension", "process")
        val manager = UnitManager()

        // when
        val actual = manager.findRefBySymbolOrSanitizeSymbol(importedUnit.symbol)

        // then
        assertEquals("_process", actual)
    }
}
