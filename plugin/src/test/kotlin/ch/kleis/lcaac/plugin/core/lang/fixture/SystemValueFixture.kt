package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaac.plugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValueWithAllocation
import ch.kleis.lcaac.plugin.core.lang.value.SystemValue

class SystemValueFixture {
    companion object {
        fun carrotSystem() = SystemValue(mutableSetOf(carrotProcessValue), mutableSetOf())
        fun carrotSystemWithAllocation() = SystemValue(mutableSetOf(carrotProcessValueWithAllocation), mutableSetOf())
    }
}
