/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EBioExchange
import ch.kleis.lcaac.core.lang.expression.EImpact
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.math.basic.BasicNumber

class SubstanceCharacterizationFixture {
    companion object {
        val propanolCharacterization = ESubstanceCharacterization(
            referenceExchange = EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            impacts = listOf(
                EImpact(QuantityFixture.oneKilogram, IndicatorFixture.climateChange),
            ),
        )

        fun substanceCharacterizationFor(substance: ESubstanceSpec<BasicNumber>): ESubstanceCharacterization<BasicNumber> =
            ESubstanceCharacterization(
                referenceExchange = EBioExchange(QuantityFixture.oneKilogram, substance),
                impacts = listOf(
                    EImpact(QuantityFixture.oneKilogram, IndicatorFixture.climateChange)
                )
            )
    }
}
