/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue

class TechnoExchangeValueFixture {
    companion object {
        val carrotTechnoExchangeValue = TechnoExchangeValue(
            QuantityValueFixture.oneKilogram,
            ProductValueFixture.carrot
        )

        val carrotTechnoExchangeValueWithAllocation = TechnoExchangeValue(
            QuantityValueFixture.oneKilogram,
            ProductValueFixture.carrot,
            QuantityValueFixture.fiftyPercent
        )

        val waterTechnoExchangeValueWithAllocation = TechnoExchangeValue(
            QuantityValueFixture.oneLitre,
            ProductValueFixture.water
        )
    }
}
