/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ProductValueFixture {
    companion object {
        fun product(s: String, unit: UnitValue<BasicNumber> = UnitValueFixture.kg()): ProductValue<BasicNumber> {
            return ProductValue(s, unit)
        }

        val salad = ProductValue("salad", UnitValueFixture.kg<BasicNumber>())
        val carrot = ProductValue("carrot", UnitValueFixture.kg<BasicNumber>())
        val water = ProductValue("water", UnitValueFixture.l<BasicNumber>())
    }
}
