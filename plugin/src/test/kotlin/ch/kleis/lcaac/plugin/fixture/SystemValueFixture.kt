package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.plugin.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaac.plugin.fixture.ProcessValueFixture.Companion.carrotProcessValueWithAllocation

class SystemValueFixture {
    companion object {
        fun carrotSystem() = SystemValue(mutableSetOf(carrotProcessValue), mutableSetOf())
        fun carrotSystemWithAllocation() = SystemValue(mutableSetOf(carrotProcessValueWithAllocation), mutableSetOf())
    }
}
