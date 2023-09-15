package ch.kleis.lcaac.plugin.core.lang.evaluator

import ch.kleis.lcaac.plugin.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.plugin.core.lang.expression.ETechnoExchange
import ch.kleis.lcaac.plugin.core.lang.fixture.*
import ch.kleis.lcaac.plugin.core.lang.value.QuantityValue
import ch.kleis.lcaac.plugin.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicOperations
import org.junit.Assert.assertEquals
import org.junit.Test

class ToValueTest {
    private val ops = BasicOperations

    @Test
    fun toValue_whenETechnoExchange() {
        // given
        val allocation = EQuantityScale(ops.pure(10.0), UnitFixture.percent)
        val expression = ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot, allocation)
        // when
        val actual = with(ToValue(BasicOperations)) { expression.toValue() }
        // then
        val allocationValue = QuantityValue(ops.pure(10.0), UnitValueFixture.percent())
        val expected =
            TechnoExchangeValue(QuantityValueFixture.oneKilogram, ProductValueFixture.carrot, allocationValue)

        assertEquals(expected, actual)
    }
}
