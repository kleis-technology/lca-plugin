/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.core.lang.value.ImpactValue
import ch.kleis.lcaac.core.lang.value.SubstanceCharacterizationValue

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
