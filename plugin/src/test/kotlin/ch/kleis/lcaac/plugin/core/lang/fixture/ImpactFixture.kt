package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.expression.EImpact
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber

object ImpactFixture {
    val oneClimateChange: EImpact<BasicNumber> = EImpact(
        quantity = QuantityFixture.oneKilogram,
        indicator = IndicatorFixture.climateChange
    )
}
