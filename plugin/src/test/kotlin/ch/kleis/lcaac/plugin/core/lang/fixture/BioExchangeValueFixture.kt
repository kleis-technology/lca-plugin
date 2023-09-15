package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.value.BioExchangeValue

class BioExchangeValueFixture {
    companion object {
        val propanolBioExchange = BioExchangeValue(
            QuantityValueFixture.oneKilogram,
            FullyQualifiedSubstanceValueFixture.propanol
        )
    }
}
