package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver(
    private val symbolTable: SymbolTable,
) {
    fun resolve(spec: ESubstanceSpec): ESubstanceCharacterization? {
        val name = spec.name
        val mType = spec.type
        val mComp: String? = spec.compartment
        val mSubcomp: String? = spec.subcompartment

        /* Required for resolve */
        if (mComp == null || mType == null) {
            return null
        }

        if (mSubcomp == null) {
            return symbolTable.getSubstanceCharacterization(name, mType, mComp)
        } else {
            val mResult = symbolTable.getSubstanceCharacterization(name, mType, mComp, mSubcomp)
            if (mResult != null) {
                return mResult
            } else {
                return symbolTable.getSubstanceCharacterization(name, mType, mComp)
            }
        }

    }
}
