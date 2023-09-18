package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
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
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    name = "eProcess",
                    impacts = listOf(
                        ImpactFixture.oneClimateChange
                    ),
                )
            )
        )
        val evaluator = Evaluator(symbolTable, ops)
        val expected = ImpactValue(
            QuantityValueFixture.oneKilogram,
            IndicatorValueFixture.climateChange,
        )

        // when
        val actual = evaluator.eval(instance).processes.first().impacts.first()

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_unresolvedSubstance_shouldBeTreatedAsTerminal() {
        // given
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val instance = EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "eProcess",
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
        )
        val evaluator = Evaluator(symbolTable, ops)
        val expected = FullyQualifiedSubstanceValue<BasicNumber>(
            "doesNotExist",
            type = SubstanceType.EMISSION,
            compartment = "water",
            subcompartment = "sea water",
            referenceUnit = UnitValue(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
        )

        // when
        val actual = evaluator.eval(instance).processes.first().biosphere.first().substance

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenTwoInstancesOfSameTemplate_thenDifferentProduct() {
        // given
        val template = TemplateFixture.carrotProduction
        val i1 = EProcessTemplateApplication(template, mapOf("q_water" to QuantityFixture.oneLitre))
        val i2 = EProcessTemplateApplication(template, mapOf("q_water" to QuantityFixture.twoLitres))
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when
        val p1 = recursiveEvaluator.eval(i1).processes.first().products.first().product
        val p2 = recursiveEvaluator.eval(i2).processes.first().products.first().product

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
        val appli = EProcessTemplateApplication(template)
        val register = Register.empty<EProcessTemplate<BasicNumber>>().plus(mapOf("carrot_production" to template))

        val symbolTable = SymbolTable(processTemplates = register)
        val recursiveEvaluator = Evaluator(symbolTable, BasicOperations)

        // when

        val p1 = recursiveEvaluator.eval(appli).processes.first().products.first().product

        // then
        assertEquals("carrot", p1.name)
    }

    @Test
    fun eval_withImplicitProcessResolution_thenCorrectSystem() {
        // given
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production" to TemplateFixture.carrotProduction
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
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
        )
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when
        val actual = recursiveEvaluator.eval(expression).processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "carrot_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre
                                ),
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
                            FromProcessRefValue(
                                name = "salad_production",
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.oneLitre,
                                ),
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
        // given
        val processTemplates: Register<EProcessTemplate<BasicNumber>> = Register.from(
            mapOf(
                "carrot_production" to TemplateFixture.carrotProduction
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = processTemplates,
        )
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
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
        )
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when
        val actual = recursiveEvaluator.eval(expression).processes

        // then
        val expected = setOf(
            ProcessValue(
                name = "salad_production",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.salad.withFromProcessRef(
                            FromProcessRefValue(
                                name = "salad_production",
                            )
                        )
                    )
                ),
                inputs = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneKilogram,
                        ProductValueFixture.carrot.withFromProcessRef(
                            FromProcessRefValue(
                                name = "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                )
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
                            FromProcessRefValue(
                                "carrot_production",
                                arguments = mapOf(
                                    "q_water" to QuantityValueFixture.twoLitres
                                ),
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
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
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
        )
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { recursiveEvaluator.eval(expression) }
        assertEquals("no process 'carrot_production' providing 'irrelevant_product' found", e.message)
    }

    @Test
    fun eval_whenNonEmptyBiosphere_thenIncludeSubstanceCharacterization() {
        // given
        val symbolTable = SymbolTable(
            substanceCharacterizations = Register.from(
                mapOf(
                    "propanol" to SubstanceCharacterizationFixture.propanolCharacterization,
                )
            ),
        )
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
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
        )
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when
        val actual = recursiveEvaluator.eval(expression).substanceCharacterizations.toSet()

        // then
        val expected = setOf(
            SubstanceCharacterizationValueFixture.propanolCharacterization
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProductUnitNotMatchProcess_shouldThrow() {
        // given
        val symbolTable = SymbolTable(
            processTemplates = Register.from(
                mapOf(
                    "carrot_production" to TemplateFixture.carrotProduction
                )
            )
        )
        val expression = EProcessTemplateApplication(
            template = EProcessTemplate(
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
        )
        val recursiveEvaluator = Evaluator(symbolTable, ops)

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) { recursiveEvaluator.eval(expression) }
        assertEquals("incompatible dimensions: length³ vs mass for product carrot", e.message)
    }
}