package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.plugin.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValue
import ch.kleis.lcaac.plugin.fixture.TechnoExchangeValueFixture.Companion.carrotTechnoExchangeValueWithAllocation
import ch.kleis.lcaac.plugin.fixture.TechnoExchangeValueFixture.Companion.waterTechnoExchangeValueWithAllocation

class ProcessValueFixture {
    companion object {
        val carrotProcessValue = ProcessValue(
            name = "carrot",
            products = listOf(carrotTechnoExchangeValue),
        )

        val carrotProcessValueWithAllocation = ProcessValue(
            name = "carrot",
            products = listOf(carrotTechnoExchangeValueWithAllocation),
            inputs = listOf(waterTechnoExchangeValueWithAllocation),
        )
    }
}
