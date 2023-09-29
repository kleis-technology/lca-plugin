/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver<Q>(
    private val symbolTable: SymbolTable<Q>,
) {
    fun resolve(spec: ESubstanceSpec<Q>): ESubstanceCharacterization<Q>? {
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null

        return spec.subCompartment?.let { subCompartment ->
            symbolTable.getSubstanceCharacterization(name, type, compartment, subCompartment)
        } ?: symbolTable.getSubstanceCharacterization(name, type, compartment)
    }
}
