package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class PartiallyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            PartiallyQualifiedSubstanceValue("propanol", UnitValueFixture.kg<BasicNumber>())
    }
}
