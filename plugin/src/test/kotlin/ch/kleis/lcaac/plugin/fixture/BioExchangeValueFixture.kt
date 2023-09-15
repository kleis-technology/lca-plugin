package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.value.BioExchangeValue

class BioExchangeValueFixture {
    companion object {
        val propanolBioExchange = BioExchangeValue(
            QuantityValueFixture.oneKilogram,
            FullyQualifiedSubstanceValueFixture.propanol
        )
    }
}
