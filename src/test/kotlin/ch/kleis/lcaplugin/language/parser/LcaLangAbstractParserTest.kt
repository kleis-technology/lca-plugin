package ch.kleis.lcaplugin.language.parser

import arrow.optics.Every
import arrow.optics.dsl.index
import arrow.optics.typeclasses.Index
import ch.kleis.lcaplugin.core.lang.RegisterException
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class LcaLangAbstractParserTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun testParse_shouldPreventDefiningTwoReferenceUnitsForTheSameDimension() {
        // given
        val file = parseFile(
            "hello", """
            unit foo1 {
                symbol = "foo1"
                dimension = "foo"
            }
            
            unit foo2 {
                symbol = "foo2"
                dimension = "foo"
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate reference units for dimensions [foo]", e.message)
    }

    @Test
    fun testParse_shouldPreventDefiningReferenceUnitForDimensionInPrelude() {
        // given
        val file = parseFile(
            "hello", """
            unit foo1 {
                symbol = "foo1"
                dimension = "mass"
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate reference units for dimensions [mass]", e.message)
    }

    @Test
    fun testParse_shouldLoadUnitAlias() {
        // given
        val file = parseFile(
            "hello", """
            unit lbs {
                symbol = "lbs"
                alias_for = 2.2 kg
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.getQuantity("lbs")
        val expect = EUnitAlias("lbs", EQuantityScale(2.2, EQuantityRef("kg")))
        assertEquals(expect, actual)
    }

    @Test
    fun testParse_shouldLoadPreludeUnits() {
        // given
        val file = parseFile(
            "hello", """
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()

        // then
        Prelude.unitsAsQuantities.getValues().onEach {
            assertNotNull(symbolTable.getQuantity(it.toString()))
        }
    }

    @Test
    fun testParse_blockUnit_shouldDeclareQuantityRef() {
        // given
        val file = parseFile(
            "hello", """
                unit fooUnitName {
                    symbol = "fooSymbol"
                    dimension = "fooDimension"
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val symbolTable = parser.load()
        val unit = "fooSymbol"

        // when
        val quantity = symbolTable.getQuantity("fooUnitName") as EUnitLiteral

        // then
        assertEquals(quantity.symbol.toString(), unit)
        assertEquals(quantity.scale, 1.0)
    }

    @Test
    fun testParse_referenceStartingWithUnderscore_shouldParse() {
        // given
        val file = parseFile(
            "hello", """
                variables {
                    _1kg = 1 kg
                }
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getBlocksOfGlobalVariables().first()
            .globalAssignmentList.first().getQuantityRef().getUID()

        // then
        val expected = "_1kg"
        assertEquals(expected, actual.name)
    }

    @Test
    fun testParse_processWithLandUse_shouldParse() {
        val file = parseFile(
            "hello", """
                process a {
                    products {
                        1 kg x
                    }
                    land_use {
                        1 kg lu
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val symbolTable = parser.load()

        // when
        val template = symbolTable.processTemplates["a"] as ProcessTemplateExpression
        val actual =
            (ProcessTemplateExpression.eProcessTemplate.body.biosphere compose
                Every.list() compose EBioExchange.substance).firstOrNull(template)

        // then
        assertEquals("lu", actual?.name)
    }

    @Test
    fun testParse_substance_shouldParseFields() {
        val subName = "co2"
        val type = SubstanceType.RESOURCE
        val compartment = "air"
        val subCompartment = "low pop"
        val file = parseFile(
            "hello", """
                substance $subName {
                    name = "carbon dioxide"
                    type = $type
                    compartment = "$compartment"
                    sub_compartment = "$subCompartment"
                    reference_unit = kg
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()

        // when
        val actual = symbolTable.getSubstanceCharacterization(
            name = subName,
            type = type,
            compartment = compartment,
            subCompartment = subCompartment,
        )!!.referenceExchange.substance

        // then
        assertEquals("co2", actual.name)
        assertEquals("carbon dioxide", actual.displayName)
        assertEquals("air", actual.compartment)
        assertEquals("low pop", actual.subCompartment)
        assertEquals(EUnitOf(EQuantityRef("kg")), actual.referenceUnit)
    }

    @Test
    fun testParse_whenDefineUnitTwice_shouldThrow() {
        val file = parseFile(
            "testParse_whenDefineUnitTwice_shouldThrow.lca",
            """
            unit foo {
                symbol = "foo"
                alias_for = 1 u
            }
            unit foo {
                symbol = "foo"
                alias_for = 10 u
            }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate global variable [foo] defined", e.message)
    }

    @Test
    fun testParse_whenDefineProcessTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                process a {
                }
                process a {
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate process [a] defined", e.message)
    }

    @Test
    fun testParse_whenDefineProductTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                process a {
                    products {
                        1 kg x
                    }
                }
                process b {
                    products {
                        1 kg x
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(RegisterException::class, null) { parser.load() }
        assertEquals("[x] is already bound", e.message)
    }

    @Test
    fun testParse_withoutPackage_thenDefaultPackageName() {
        // given
        val file = parseFile(
            "hello", """
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getPackageName()

        // then
        val expected = "default"
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_whenDefineGlobalVariableTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                variables {
                    x = 1 kg
                    x = 3 l
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate global variable [x] defined", e.message)
    }

    @Test
    fun testParse_whenDefineSameSubstanceTwice_shouldThrow() {
        // given
        val file = parseFile(
            "hello", """
                substance a {
                    name = "first"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg
                }
                substance a {
                    name = "second"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg 
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Duplicate substance [a_compartment_Resource] defined", e.message)
    }

    @Test
    fun testParse_whenDefineSameSubstanceDifferentSubCompartments_shouldNotThrow() {
        // given
        val file = parseFile(
            "hello", """
                substance a {
                    name = "first"
                    type = Resource
                    compartment = "compartment"
                    sub_compartment = "subCompartment"
                    reference_unit = kg
                }
                substance a {
                    name = "second"
                    type = Resource
                    compartment = "compartment"
                    reference_unit = kg 
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when, then should not throw
        parser.load()
    }

    @Test
    fun testParse_whenDefineUnitAndVariableWithSameName_shouldThrow() {
        // given
        // unit p from prelude
        val file = parseFile(
            "hello", """
                process p {
                    variables {
                        p = 1 kg
                    }
                    products {
                        1 kg productName
                    }
                }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when/then
        val e = assertFailsWith(EvaluatorException::class, null) { parser.load() }
        assertEquals("Conflict between local variable(s) [p] and a global definition.", e.message)
    }

    @Test
    fun testParse_withPackage_shouldReturnGivenPackageName() {
        // given
        val file = parseFile(
            "hello", """
                package a.b.c
        """.trimIndent()
        ) as LcaFile

        // when
        val actual = file.getPackageName()

        // then
        val expected = "a.b.c"
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_simpleProcess() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                products {
                    1 kg carrot
                }
                inputs {
                    10 l water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getTemplate("a")!!

        // then
        val preludeSymbolTable = SymbolTable(
            data = Prelude.unitsAsData
        )
        val expected = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            EProcess(
                name = "a",
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(
                        EQuantityScale(1.0, EQuantityRef("kg")),
                        EProductSpec(
                            "carrot",
                            EUnitOf(
                                EQuantityClosure(
                                    preludeSymbolTable,
                                    EQuantityScale(1.0, EQuantityRef("kg")),
                                )
                            )
                        ),
                    ),
                ),
                inputs = listOf(
                    ETechnoExchange(
                        EQuantityScale(10.0, EQuantityRef("l")),
                        EProductSpec("water"),
                    ),
                ),
                biosphere = emptyList(),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_unitExpression_div() {
        // given
        val subName = "a"
        val type = SubstanceType.RESOURCE
        val compartment = "compartment"
        val file = parseFile(
            "hello", """
            substance $subName {
                name = "a"
                type = $type
                compartment = "$compartment"
                reference_unit = x/y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstanceCharacterization(
            name = subName,
            type = type,
            compartment = compartment,
        )!!.referenceExchange.substance
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitOf(
            EQuantityDiv(
                EQuantityRef("x"),
                EQuantityRef("y"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_unitExpression_mul() {
        // given
        val subName = "a"
        val type = SubstanceType.RESOURCE
        val compartment = "compartment"
        val file = parseFile(
            "hello", """
            substance $subName {
                name = "a"
                type = $type
                compartment = "$compartment"
                reference_unit = x*y
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val substance = symbolTable.getSubstanceCharacterization(
            name = subName,
            type = type,
            compartment = compartment,
        )!!.referenceExchange.substance
        val actual = substance.referenceUnit!!

        // then
        val expected = EUnitOf(
            EQuantityMul(
                EQuantityRef("x"),
                EQuantityRef("y"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_quantityExpression_div() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                inputs {
                    10 x / (20 y) water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
            ProcessTemplateExpression.eProcessTemplate.body.inputs.index(Index.list(), 0) compose
                ETechnoExchange.quantity
            ).getOrNull(template)!!

        // then
        val expected = EQuantityDiv(
            EQuantityScale(10.0, EQuantityRef("x")),
            EQuantityScale(20.0, EQuantityRef("y"))
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_quantityExpression_mul() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                inputs {
                    10 x * (20 y) water
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate("a")!!
        val actual = (
            ProcessTemplateExpression.eProcessTemplate.body.inputs.index(Index.list(), 0) compose
                ETechnoExchange.quantity
            ).getOrNull(template)!!

        // then
        val expected = EQuantityMul(
            EQuantityScale(10.0, EQuantityRef("x")),
            EQuantityScale(20.0, EQuantityRef("y"))
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_withConstrainedProduct() {
        // given
        val file = parseFile(
            "hello", """
            process a {
                products {
                    1 kg carrot
                }
                inputs {
                    10 l water from water_proc(x = 3 l)
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val expression = symbolTable.getTemplate("a")!!
        val actual =
            ProcessTemplateExpression.eProcessTemplate.body.inputs.getAll(expression).flatten()

        // then
        val expected = listOf(
            ETechnoExchange(
                EQuantityScale(10.0, EQuantityRef("l")),
                EProductSpec(
                    "water",
                    fromProcessRef = FromProcess(
                        "water_proc",
                        mapOf("x" to EQuantityScale(3.0, EQuantityRef("l"))),
                    ),
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_substanceWithImpacts_shouldReturnASubstanceCharacterization() {
        // given
        val name = "phosphate"
        val type = SubstanceType.RESOURCE
        val compartment = "phosphate compartment"
        val subCompartment = "phosphate sub-compartment"
        val file = parseFile(
            "substances", """
            substance $name {
                name = "phosphate"
                type = $type
                compartment = "$compartment"
                sub_compartment = "$subCompartment"
                reference_unit = kg
                
                impacts {
                    1 kg climate_change
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()
        val actual = symbolTable.getSubstanceCharacterization(
            name = name,
            type = type,
            compartment = compartment,
            subCompartment = subCompartment
        )

        // then
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EQuantityRef("kg"),
                ESubstanceSpec(
                    name = "phosphate",
                    type = SubstanceType.RESOURCE,
                    compartment = "phosphate compartment",
                    subCompartment = "phosphate sub-compartment",
                    referenceUnit = EUnitOf(EQuantityRef("kg")),
                ),
            ),
            impacts = listOf(
                EImpact(
                    EQuantityScale(1.0, EQuantityRef("kg")),
                    EIndicatorSpec("climate_change"),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_productWithoutAllocation_should_return_100percent_allocation() {
        // given
        val file = parseFile(
            "carrot", """
            process carrot {
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body).products[0]
        val preludeSymbolTable = SymbolTable(
            data = Prelude.unitsAsData
        )
        val expected = ETechnoExchange(
            EQuantityScale(1.0, EQuantityRef("kg")),
            EProductSpec(
                "carrot",
                EUnitOf(EQuantityClosure(preludeSymbolTable, EQuantityScale(1.0, EQuantityRef("kg"))))
            ),
            EQuantityScale(100.0, EUnitLiteral(UnitSymbol.of("percent"), 0.01, Dimension.None))
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_productWithAllocation() {
        // given
        val file = parseFile(
            "carrot", """
            process carrot {
                products {
                    1 kg carrot allocate 10 percent
                }
            }
        """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        // when
        val symbolTable = parser.load()
        // then
        val actual = ((symbolTable.processTemplates["carrot"] as EProcessTemplate).body).products[0]
        val preludeSymbolTable = SymbolTable(
            data = Prelude.unitsAsData
        )
        val expect = ETechnoExchange(
            EQuantityScale(1.0, EQuantityRef("kg")),
            EProductSpec(
                "carrot",
                EUnitOf(EQuantityClosure(preludeSymbolTable, EQuantityScale(1.0, EQuantityRef("kg"))))
            ),
            EQuantityScale(10.0, EQuantityRef("percent"))
        )
        assertEquals(expect, actual)
    }

    @Test
    fun testParse_whenDivideThenMultiply_shouldFactorCorrectly() {
        // given
        val file = parseFile(
            "maths", """
                    process p {
                        variables {
                            r = 20 u / 10 u * 2 u
                        }
                        products {
                            1 p product
                        }
                        emissions {
                            r result
                        }
                    }
                """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val expected: QuantityExpression = EQuantityMul(
            EQuantityDiv(
                EQuantityScale(20.0, EQuantityRef("u")),
                EQuantityScale(10.0, EQuantityRef("u"))
            ),
            EQuantityScale(2.0, EQuantityRef("u"))
        )

        // when
        val symbolTable = parser.load()

        // then
        val template: EProcessTemplate = symbolTable.processTemplates["p"]
            ?: throw Exception("template fetching barfed")
        val local = template.locals["r"] ?: throw Exception("locals barfed")
        assertEquals(expected, local)

    }

    @Test
    fun testParse_whenMultiplyThenDivide_shouldFactorCorrectly() {
        // given
        val file = parseFile(
            "maths", """
                    process p {
                        variables {
                            r = 10 u * 2 u / 20 u
                        }
                        products {
                            1 p product
                        }
                        emissions {
                            r result
                        }
                    }
                """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val expected: QuantityExpression = EQuantityDiv(
            EQuantityMul(
                EQuantityScale(10.0, EQuantityRef("u")),
                EQuantityScale(2.0, EQuantityRef("u"))
            ),
            EQuantityScale(20.0, EQuantityRef("u"))
        )

        // when
        val symbolTable = parser.load()

        // then
        val template: EProcessTemplate = symbolTable.processTemplates["p"]
            ?: throw Exception("template fetching barfed")
        val local = template.locals["r"] ?: throw Exception("locals barfed")
        assertEquals(expected, local)

    }

    @Test
    fun testParse_whenDimensionPow_thenScaleUntouched() {
        // given
        val file = parseFile(
            "maths", """
                package testParse_whenDimensionPow_thenScaleUntouched

                variables {
                    r = 10 m^2
                }
            """.trimIndent()
        ) as LcaFile
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )
        val expected: QuantityExpression = EQuantityScale(
            10.0,
            EQuantityPow(
                EQuantityRef("m"),
                2.0
            )
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.data["r"]!!
        assertEquals(expected, actual)
    }

    @Test
    fun testParse_whenQuantityExpressionPow_thenScaleIsTouched() {
        // given
        val file = parseFile(
            "maths", """
                package testParse_whenDimensionPow_thenScaleUntouched

                variables {
                    r = (10 m)^2
                }
            """.trimIndent()
        ) as LcaFile

        val expected: QuantityExpression = EQuantityPow(
            EQuantityScale(
                10.0,
                EQuantityRef("m")
            ),
            2.0
        )
        val parser = LcaLangAbstractParser(
            sequenceOf(file)
        )

        // when
        val symbolTable = parser.load()

        // then
        val actual = symbolTable.data["r"]!!
        assertEquals(expected, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
