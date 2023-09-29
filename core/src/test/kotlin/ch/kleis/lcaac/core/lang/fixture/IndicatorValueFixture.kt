/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class IndicatorValueFixture {
    companion object {
        val climateChange = IndicatorValue("Climate Change", UnitValueFixture.kg<BasicNumber>())
    }
}
