package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.PkgResolverFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class ReduceLabelSelectorsTest {
    private val ops = BasicOperations

    @Test
    fun reduce_whenPassingByArguments() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            quantity = QuantityFixture.oneKilogram,
                            product = EProductSpec(
                                name = "carrot",
                                referenceUnit = QuantityFixture.oneKilogram,
                                from = FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
            mapOf("geo" to EStringLiteral("FR")),
        )
        val pkg = EPackage.empty<BasicNumber>()
        val reduceLabelSelectors = ReduceLabelSelectors(pkg, PkgResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
            mapOf("geo" to EStringLiteral("FR")),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByDefaultParams() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val pkg = EPackage.empty<BasicNumber>()
        val reduceLabelSelectors = ReduceLabelSelectors(pkg, PkgResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                params = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByLocals() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                locals = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val pkg = EPackage.empty<BasicNumber>()
        val reduceLabelSelectors = ReduceLabelSelectors(pkg, PkgResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                locals = mapOf("geo" to EStringLiteral("GLO")),
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("GLO"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByGlobalVariables() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    name = "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val pkg = EPackage<BasicNumber>(
            data = DataRegister(mapOf(DataKey("geo") to EStringLiteral("FR")))
        )
        val reduceLabelSelectors = ReduceLabelSelectors(pkg, PkgResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPassingByProcessLabels() {
        // given
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        val pkg = EPackage.empty<BasicNumber>()
        val reduceLabelSelectors = ReduceLabelSelectors(pkg, PkgResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = reduceLabelSelectors.apply(instance)

        // then
        val expected = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "salad_production",
                    labels = mapOf("geo" to EStringLiteral("FR")),
                    inputs = listOf(
                        ETechnoExchange(
                            QuantityFixture.oneKilogram,
                            EProductSpec(
                                "carrot",
                                QuantityFixture.oneKilogram,
                                FromProcess(
                                    name = "carrot_production",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                )
                            )
                        )
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }
}
