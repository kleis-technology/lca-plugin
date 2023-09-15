package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.expression.SubstanceType
import ch.kleis.lcaac.plugin.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber

class FullyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            FullyQualifiedSubstanceValue("propanol", SubstanceType.RESOURCE, "air", null, UnitValueFixture.kg<BasicNumber>())
    }
}
