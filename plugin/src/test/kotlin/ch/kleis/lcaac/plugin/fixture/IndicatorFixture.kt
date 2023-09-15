package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.expression.EIndicatorSpec

class IndicatorFixture {
    companion object {
        val climateChange = EIndicatorSpec(
            "Climate Change",
            QuantityFixture.oneKilogram,
        )
    }
}
