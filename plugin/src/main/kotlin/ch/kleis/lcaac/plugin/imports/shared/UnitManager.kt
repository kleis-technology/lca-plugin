package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol
import com.intellij.openapi.diagnostic.Logger

/*
    Stateful.
 */
class UnitManager {
    private val importedUnitsByRef: MutableMap<String, ImportedUnit> = mutableMapOf()
    private val preludeUnitsByRef = Prelude.unitMap<BasicNumber>()
    private val knownUnitRefs: MutableSet<String> = preludeUnitsByRef.keys.toMutableSet()
    var numberOfAddInvocations: Int = 0
        private set


    companion object {
        val LOG = Logger.getInstance(UnitManager::class.java)
    }

    /*
        Returns
        - the original unit if it is effectively added
        - null otherwise
     */
    fun add(unit: ImportedUnit): ImportedUnit? {
        numberOfAddInvocations += 1
        if (isKnown(unit)) return null
        val ref = unit.ref()
        importedUnitsByRef[ref] = unit
        knownUnitRefs.add(ref)
        return unit
    }

    private fun isKnown(unit: ImportedUnit): Boolean {
        return knownUnitRefs.contains(unit.ref())
    }

    private fun findRefBySymbol(symbol: String): String? {
        return importedUnitsByRef.values.firstOrNull { it.symbol == symbol }?.ref()
            ?: preludeUnitsByRef.entries.firstOrNull { it.value.symbol.toString() == symbol }?.key
    }

    fun findRefBySymbolOrSanitizeSymbol(symbol: String): String {
        return findRefBySymbol(symbol) ?: sanitizeSymbol(symbol)
    }
}
