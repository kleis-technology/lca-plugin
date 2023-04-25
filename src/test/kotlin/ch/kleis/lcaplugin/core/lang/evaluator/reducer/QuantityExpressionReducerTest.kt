package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.pow

class QuantityExpressionReducerTest {
    /*
        QUANTITIES
     */

    @Test
    fun reduce_whenScale_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.oneKilogram
        val quantity = EQuantityScale(2.0, innerQuantity)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = QuantityFixture.twoKilograms
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScaleAndUnboundRef_shouldDoNothing() {
        // given
        val innerQuantity = EQuantityRef("a")
        val quantity = EQuantityScale(2.0, innerQuantity)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(quantity, actual)
    }

    @Test
    fun reduce_whenLiteralWithUnitOf_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.twoKilograms
        val unit = EUnitOf(innerQuantity)
        val quantity = EQuantityLiteral(1.0, unit)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = EQuantityLiteral(1.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReduceUnit() {
        // given
        val quantityEnvironment = Register.empty<QuantityExpression>()
        val unitEnvironment = Register<UnitExpression>(
            hashMapOf(
                Pair("kg", UnitFixture.kg)
            )
        )
        val quantity = EQuantityLiteral(1.0, EUnitRef("kg"))
        val reducer = QuantityExpressionReducer(quantityEnvironment, unitEnvironment)

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = EQuantityLiteral(1.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameDimension_shouldAddAndSelectBiggestScale() {
        // given
        val a = EQuantityLiteral(2.0, UnitFixture.kg)
        val b = EQuantityLiteral(1000.0, UnitFixture.g)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        val expected = EQuantityLiteral(3.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityLiteral(2.0, UnitFixture.kg)
        val b = EQuantityLiteral(1000.0, UnitFixture.m)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when/then
        try {
            reducer.reduce(EQuantityAdd(a, b))
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("incompatible dimensions: mass vs length", e.message)
        }
    }

    @Test
    fun reduce_sub_whenSameDimension_shouldAddAndSelectBiggestScale() {
        // given
        val a = EQuantityLiteral(2.0, UnitFixture.kg)
        val b = EQuantityLiteral(1000.0, UnitFixture.g)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        val expected = EQuantityLiteral(1.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityLiteral(2.0, UnitFixture.kg)
        val b = EQuantityLiteral(1000.0, UnitFixture.m)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when/then
        try {
            reducer.reduce(EQuantitySub(a, b))
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("incompatible dimensions: mass vs length", e.message)
        }
    }

    @Test
    fun reduce_mul_shouldMultiply() {
        // given
        val a = EQuantityLiteral(2.0, UnitFixture.person)
        val b = EQuantityLiteral(2.0, UnitFixture.km)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityLiteral(
            4.0,
            EUnitLiteral(
                "person.km",
                1.0 * 1000.0,
                Dimension.None.multiply(DimensionFixture.length)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_shouldMultiply() {
        // given
        val a = EQuantityLiteral(4.0, UnitFixture.km)
        val b = EQuantityLiteral(2.0, UnitFixture.hour)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityLiteral(
            2.0,
            EUnitLiteral(
                "km/hour",
                1000.0 / 3600.0,
                DimensionFixture.length.divide(DimensionFixture.time)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_pow_shouldExponentiate() {
        // given
        val a = EQuantityLiteral(4.0, UnitFixture.km)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityPow(a, 2.0))

        // then
        val expected = EQuantityLiteral(
            16.0,
            EUnitLiteral(
                "km^(2.0)",
                1e6,
                DimensionFixture.length.pow(2.0)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnvironment() {
        // given
        val a = EQuantityRef("a")
        val reducer = QuantityExpressionReducer(
            Register(
                hashMapOf(
                    Pair("a", EQuantityLiteral(1.0, UnitFixture.kg))
                )
            ),
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(a)

        // then
        val expected = EQuantityLiteral(1.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    /*
        UNITS
     */

    @Test
    fun reduce_whenUnitComposition_shouldReturnEUnitLiteral() {
        // given
        val kg = EUnitLiteral("kg", 1.0, Dimension.of("mass"))
        val quantityConversion = EQuantityLiteral(2.2, kg)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )
        // when
        val actual = reducer.reduceUnit(unitComposition)
        // then
        val expect = EUnitLiteral("lbs", scale = 2.2, Dimension.of("mass"))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitComposition_shouldRespectScaling() {
        // given
        val g = EUnitLiteral("g", 1.0E-3, Dimension.of("mass"))
        val quantityConversion = EQuantityLiteral(2200.0, g)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )
        // when
        val actual = reducer.reduceUnit(unitComposition)
        // then
        val expect = EUnitLiteral("lbs", scale = 2.2, Dimension.of("mass"))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitOf_shouldReduceQuantity() {
        // given
        val quantity = QuantityFixture.twoKilograms
        val unit = EUnitOf(quantity)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduceUnit(unit)

        // then
        val expected = UnitFixture.kg
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitClosure_shouldReduceWithGivenTable() {
        // given
        val symbolTable = SymbolTable(
            units = Register(
                "a" to UnitFixture.kg
            )
        )
        val unit = EUnitClosure(symbolTable, EUnitRef("a"))
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register(
                "a" to UnitFixture.l
            )
        )

        // when
        val actual = reducer.reduceUnit(unit)

        // then
        val expected = UnitFixture.kg
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReturnSame() {
        // given
        val kg = UnitFixture.kg
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduceUnit(kg)

        // then
        assertEquals(kg, actual)
    }

    @Test
    fun reduce_whenDiv_shouldDivide() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduceUnit(EUnitDiv(kg, l))

        // then
        val expected = EUnitLiteral(
            "kg/l",
            1.0 / 1.0e-3,
            DimensionFixture.mass.divide(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenMul_shouldMultiply() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduceUnit(EUnitMul(kg, l))

        // then
        val expected = EUnitLiteral(
            "kg.l",
            1.0 * 1.0e-3,
            DimensionFixture.mass.multiply(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPow_shouldPow() {
        // given
        val m = UnitFixture.m
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            Register.empty(),
        )

        // when
        val actual = reducer.reduceUnit(EUnitPow(m, 2.0))

        // then
        val expected = EUnitLiteral(
            "m^(2.0)",
            1.0.pow(2.0),
            DimensionFixture.length.pow(2.0),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnv() {
        // given
        val ref = EUnitRef("kg")
        val units = Register<UnitExpression>(
            hashMapOf(
                Pair("kg", UnitFixture.kg)
            )
        )
        val reducer = QuantityExpressionReducer(
            Register.empty(),
            units,
        )

        // when
        val actual = reducer.reduceUnit(ref)

        // then
        assertEquals(UnitFixture.kg, actual)
    }
}
