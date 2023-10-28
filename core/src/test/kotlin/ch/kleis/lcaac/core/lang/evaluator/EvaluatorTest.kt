package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.ProcessTemplateRegister
import ch.kleis.lcaac.core.lang.register.SubstanceCharacterizationRegister
import ch.kleis.lcaac.core.lang.register.SubstanceKey
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EvaluatorTest {
    private val ops = BasicOperations

    @Test
    fun eval_processWithImpacts_shouldReduceImpacts() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "eProcess",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
                ),
                impacts = listOf(
                    ImpactFixture.oneClimateChange
                ),
            )
        )
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("eProcess") to template,
                )
            )
        )
        val resolver = ResolverFixture.alwaysResolveTo(pkg)
        val evaluator = Evaluator(resolver, ops)
        val expected = ImpactValue(
            QuantityValueFixture.oneKilogram,
            IndicatorValueFixture.climateChange,
        )

        // when
        val actual = evaluator.trace("eProcess").getEntryPoint().impacts.first()

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_unresolvedSubstance_shouldBeTreatedAsTerminal() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                "eProcess",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, EProductSpec("p", QuantityFixture.oneKilogram))
                ),
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram,
                        ESubstanceSpec(
                            "doesNotExist",
                            "doesNotExist",
                            SubstanceType.EMISSION,
                            "water",
                            "sea water"
                        )
                    )
                ),
            )
        )
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("eProcess") to template
                )
            )
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)
        val expected = FullyQualifiedSubstanceValue<BasicNumber>(
            "doesNotExist",
            type = SubstanceType.EMISSION,
            compartment = "water",
            subcompartment = "sea water",
            referenceUnit = UnitValue(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass),
            PackageValue(EPackage.DEFAULT_PKG_NAME),
        )

        // when
        val actual = evaluator.trace("eProcess").getEntryPoint().biosphere.first().substance

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenTwoInstancesOfSameTemplate_thenDifferentProduct() {
        // given
        val template = TemplateFixture.carrotProduction
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("carrot_production") to template,
                )
            )
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val p1 = evaluator.trace("carrot_production", mapOf("q_water" to QuantityFixture.oneLitre))
            .getEntryPoint().products.first().product
        val p2 = evaluator.trace("carrot_production", mapOf("q_water" to QuantityFixture.twoLitres))
            .getEntryPoint().products.first().product

        // then
        assertEquals(p1.name, p2.name)
        assertEquals(p1.referenceUnit, p2.referenceUnit)
        assertNotEquals(p1, p2)
    }

    @Test
    @Timeout(2)
    fun eval_whenAProductAsACycle_thenItShouldEnd() {
        // given
        val template = TemplateFixture.cyclicProduction
        val register = ProcessTemplateRegister(mapOf(ProcessKey("carrot_production") to template))

        val pkg = EPackage(processTemplates = register)
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), BasicOperations)

        // when

        val p1 = evaluator.trace("carrot_production").getEntryPoint().products.first().product

        // then
        assertEquals("carrot", p1.name)
    }

    @Test
    fun eval_withImplicitProcessResolution_thenCorrectSystem() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                        )
                    )
                ),
            )
        )
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("carrot_production") to TemplateFixture.carrotProduction,
                    ProcessKey("salad_production") to template,
                )
            ),
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = evaluator.trace("salad_production").getSystemValue().processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre
                                ),
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
            ),
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessValue(
                                name = "salad_production",
                                pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre,
                                ),
                                pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                            )
                        )
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenExistsFromProcessRef_thenCorrectSystem() {
        val template = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "carrot",
                            UnitFixture.kg,
                            FromProcess(
                                "carrot_production",
                                MatchLabels(emptyMap()),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
            ),
        )
        // given
        val processTemplates = ProcessTemplateRegister(
            mapOf(
                ProcessKey("carrot_production") to TemplateFixture.carrotProduction,
                ProcessKey("salad_production") to template,
            )
        )
        val pkg = EPackage(
            processTemplates = processTemplates,
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = evaluator.trace("salad_production").getSystemValue().processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessValue(
                                name = "salad_production",
                                pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                ),
                                pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                            )
                        )
                    )
                ),
            ),
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessValue(
                                "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                ),
                                pkg = PackageValue(EPackage.DEFAULT_PKG_NAME),
                            ),
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.twoLitres,
                        ProductValueFixture.water
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductDoesNotMatchProcess_shouldThrow() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        EProductSpec(
                            "irrelevant_product",
                            UnitFixture.kg,
                            FromProcess(
                                "carrot_production",
                                MatchLabels(emptyMap()),
                                mapOf("q_water" to QuantityFixture.twoLitres),
                            ),
                        )
                    )
                ),
            )
        )
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "salad_production" to template,
                ).mapKeys { ProcessKey(it.key) }
            )
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { evaluator.trace("salad_production") }
        assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
    }

    @Test
    fun eval_whenNonEmptyBiosphere_thenIncludeSubstanceCharacterization() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram, ESubstanceSpec(
                            "propanol",
                            compartment = "air",
                            type = SubstanceType.RESOURCE,
                        )
                    )
                ),
            )
        )
        val pkg = EPackage(
            substanceCharacterizations = SubstanceCharacterizationRegister(
                mapOf(
                    SubstanceKey(
                        "propanol",
                        SubstanceType.RESOURCE,
                        "air"
                    ) to SubstanceCharacterizationFixture.propanolCharacterization,
                )
            ),
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    ProcessKey("carrot_production") to template,
                )
            )
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when
        val actual = evaluator.trace("carrot_production").getSystemValue().substanceCharacterizations

        // then
        val expected = setOf(
            SubstanceCharacterizationValueFixture.propanolCharacterization
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductUnitNotMatchProcess_shouldThrow() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "salad_production",
                products = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.salad,
                    )
                ),
                inputs = listOf(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.carrot,
                    )
                ),
            )
        )
        val pkg = EPackage(
            processTemplates = ProcessTemplateRegister(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction,
                    "salad_production" to template,
                ).mapKeys { ProcessKey(it.key) }
            )
        )
        val evaluator = Evaluator(ResolverFixture.alwaysResolveTo(pkg), ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { evaluator.trace("salad_production") }
        assertEquals("incompatible dimensions: length³ vs mass for product carrot", e.message)
    }
}
