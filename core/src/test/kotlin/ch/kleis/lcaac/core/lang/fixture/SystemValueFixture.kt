/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaac.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValueWithAllocation
import ch.kleis.lcaac.core.lang.value.SystemValue

class SystemValueFixture {
    companion object {
        fun carrotSystem() = SystemValue(mutableSetOf(carrotProcessValue), mutableSetOf())
        fun carrotSystemWithAllocation() = SystemValue(mutableSetOf(carrotProcessValueWithAllocation), mutableSetOf())
    }
}
