package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.value.IndicatorValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber

class IndicatorValueFixture {
    companion object {
        val climateChange = IndicatorValue("Climate Change", UnitValueFixture.kg<BasicNumber>())
    }
}
