package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber

class PartiallyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            PartiallyQualifiedSubstanceValue("propanol", UnitValueFixture.kg<BasicNumber>())
    }
}
