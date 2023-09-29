/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EProductSpec

class ProductFixture {
    companion object {
        val carrot = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        val salad = EProductSpec(
            "salad",
            QuantityFixture.oneKilogram,
        )
        val water = EProductSpec(
            "water",
            QuantityFixture.oneLitre,
        )
    }
}
