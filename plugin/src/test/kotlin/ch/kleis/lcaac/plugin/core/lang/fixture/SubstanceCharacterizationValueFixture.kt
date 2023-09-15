package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.plugin.core.lang.value.ImpactValue
import ch.kleis.lcaac.plugin.core.lang.value.SubstanceCharacterizationValue

class SubstanceCharacterizationValueFixture {
    companion object {
        val propanolCharacterization = SubstanceCharacterizationValue(
            referenceExchange = BioExchangeValue(
                QuantityValueFixture.oneKilogram,
                FullyQualifiedSubstanceValueFixture.propanol
            ),
            impacts = listOf(
                ImpactValue(QuantityValueFixture.oneKilogram, IndicatorValueFixture.climateChange),
            ),
        )
    }
}
