/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EBioExchange
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.ETechnoExchange

class ProcessFixture {
    companion object {
        val carrotProduction = EProcess(
            name = "carrot_production",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(QuantityFixture.oneLitre, ProductFixture.water),
            ),
            biosphere = listOf(
                EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            ),
            impacts = listOf(
                ImpactFixture.oneClimateChange
            )
        )
    }
}
