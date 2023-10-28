package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.EQuantityAdd
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.lang.value.FromProcessValue
import ch.kleis.lcaac.core.lang.value.PackageValue
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ReduceTest {
    private val ops = BasicOperations
    
    @Test
    fun eval_whenInstanceOfProcessTemplate_shouldEvaluateToProcessValue() {
        // given
        val template = TemplateFixture.carrotProduction
        val instance = EProcessTemplateApplication(
            template, mapOf(
            Pair(
                "q_water", EQuantityAdd(
                QuantityFixture.oneLitre,
                QuantityFixture.oneLitre,
            )
            )
        )
        )
        val pkg = EPackage.empty<BasicNumber>()
        val reduceAndComplete = Reduce(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = with(ToValue(BasicOperations)) { reduceAndComplete.apply(instance).toValue() }

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessValue(
                            name = "carrot_production",
                            arguments = mapOf("q_water" to QuantityValueFixture.twoLitres),
                            pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                        )
                    ),
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoLitres,
                    ProductValueFixture.water,
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProcessTemplate_shouldAutomaticallyInstantiateWithoutArguments() {
        // given
        val template = EProcessTemplateApplication(TemplateFixture.carrotProduction, emptyMap())
        val pkg = EPackage.empty<BasicNumber>()
        val reduceAndComplete = Reduce(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = with(ToValue(BasicOperations)) { reduceAndComplete.apply(template).toValue() }

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessValue(
                            name = "carrot_production",
                            arguments = mapOf("q_water" to QuantityValueFixture.oneLitre),
                            pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                        )
                    )
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneLitre,
                    ProductValueFixture.water
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenContainsUnboundedReference_shouldThrow() {
        // given
        val template = EProcessTemplateApplication(TemplateFixture.withUnboundedRef, emptyMap())
        val pkg = EPackage.empty<BasicNumber>()
        val reduceAndComplete = Reduce(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when/then
        assertFailsWith(
            EvaluatorException::class,
            "unbounded references: [q_carrot, q_water]"
        ) { reduceAndComplete.apply(template) }
    }
}
