package ch.kleis.lcaplugin.e2e

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EQuantityLiteral
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.test.assertFailsWith

class E2ETest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    fun test_substanceResolution() {
        val pkgName = "e2e.test_substanceResolution"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg b(compartment="compartment")
                    }
                }
                
                substance b {
                    name = "b"
                    type = Emission
                    compartment = "compartment"
                    reference_unit = kg
                    
                    impacts {
                        1 kg co2
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.getElements().first()
                val input = result.controllablePorts.getElements().first()
                val cf = result.value(output, input)

                assertEquals("a from p{}", output.getDisplayName())
                assertEquals(1.0, cf.output.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.output.quantity().unit)

                assertEquals("co2", input.getDisplayName())
                assertEquals(1.0, cf.input.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    fun test_meta_whenKeywordAsKey() {
        // given
        val pkgName = "e2e.test_meta_whenKeywordAsKey"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
                package $pkgName
                
                process p {
                    meta {
                        "unit" = "a"
                        "process" = "b"
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile

        // when
        val actual = file.getProcesses().first().getBlockMetaList().first().metaAssignmentList

        // then
        assertEquals("unit", actual[0].name)
        assertEquals("a", actual[0].getValue())
        assertEquals("process", actual[1].name)
        assertEquals("b", actual[1].getValue())
    }

    fun test_operationPriority() {
        // given
        val pkgName = "e2e.test_operationPriority"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process p {
                variables {
                    q = 2 m/kg
                }
                products {
                    1 kg out
                }
                inputs {
                    3 kg * q in
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.getElements().first()
                val input = result.controllablePorts.getElements().first()
                val cf = result.value(output, input)

                assertEquals("out from p{}", output.getDisplayName())
                assertEquals(1.0, cf.output.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.output.quantity().unit)

                assertEquals("in", input.getDisplayName())
                assertEquals(6.0, cf.input.quantity().amount)
                assertEquals(DimensionFixture.length.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    fun test_twoInstancesSameTemplate_whenOneImplicit() {
        // given
        val pkgName = "e2e.test_twoInstancesSameTemplate_whenOneImplicit"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * (1 kg/m2) co2
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                assertEquals("office from office{}", output.getDisplayName())
                assertEquals(1.0, cf.output.quantity().amount)
                assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                assertEquals("co2", input.getDisplayName())
                assertEquals(3.0, cf.input.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    fun test_twoInstancesSameTemplate_whenExplicit() {
        // given
        val pkgName = "e2e.test_twoInstancesSameTemplate_whenExplicit"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk from desk( size = 1 m2 )
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * (1 kg/m2) co2
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                assertEquals("office from office{}", output.getDisplayName())
                assertEquals(1.0, cf.output.quantity().amount)
                assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                assertEquals("co2", input.getDisplayName())
                assertEquals(3.0, cf.input.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    fun test_manyInstancesSameTemplate() {
        // given
        val pkgName = "e2e.test_manyInstancesSameTemplate"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process office {
                products {
                    1 piece office
                }

                inputs {
                    1 piece desk
                    1 piece desk from desk( size = 1 m2 )
                    1 piece desk from desk( size = 1 m2, density = 1 kg/m2 )
                    1 piece desk from desk( size = 1 m2, density = 2 kg/m2 )
                    1 piece desk from desk( size = 2 m2, density = 2 kg/m2 )
                    1 piece desk from desk( size = 2 m2, density = 1 kg/m2 )
                    1 piece desk from desk( size = 2 m2 )
                }
            }
            
            process desk {
                params {
                    size = 1 m2
                    density = 1 kg/m2
                }

                products {
                    1 piece desk
                }

                emissions {
                    size * density co2
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["office"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output = result.observablePorts.get("office from office{}")
                val input = result.controllablePorts.get("co2")
                val cf = result.value(output, input)

                assertEquals("office from office{}", output.getDisplayName())
                assertEquals(1.0, cf.output.quantity().amount)
                assertEquals(Dimension.None.getDefaultUnitValue(), cf.output.quantity().unit)

                assertEquals("co2", input.getDisplayName())
                assertEquals(13.0, cf.input.quantity().amount)
                assertEquals(DimensionFixture.mass.getDefaultUnitValue(), cf.input.quantity().unit)
            }
        }
    }

    fun test_allocate() {
        // given
        val pkgName = "e2e.test_allocate"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out1 allocate 90 percent
                    1 kg out2 allocate 10 percent
                }
                inputs {
                    1 kg in
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))

        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output1 = result.observablePorts.getElements()[0]
                val output2 = result.observablePorts.getElements()[1]
                val input = result.controllablePorts.getElements().first()
                val cf1 = result.value(output1, input)
                val cf2 = result.value(output2, input)

                val delta = 1E-9
                assertEquals(0.9, cf1.input.quantity().amount, delta)
                assertEquals(0.1, cf2.input.quantity().amount, delta)
            }
        }
    }

    fun test_allocate_whenOneProduct_allocateIsOptional() {
        // given
        val pkgName = "e2e.test_allocate_whenOneProduct_allocateIsOptional"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        // when
        val symbolTable = parser.load()
        val actual =
                (((symbolTable.processTemplates["p"] as EProcessTemplate).body).products[0].allocation as EQuantityLiteral).amount
        // then
        assertEquals(100.0, actual)
    }

    fun test_allocate_whenSecondaryBlock_EmptyBlockIsAllowed() {
        // given
        val pkgName = "e2e.test_allocate_whenSecondaryBlock_EmptyBlockIsAllowed"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
                products {
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        // when
        val symbolTable = parser.load()
        val actual =
                (((symbolTable.processTemplates["p"] as EProcessTemplate).body).products[0].allocation as EQuantityLiteral).amount
        // then
        assertEquals(100.0, actual)
    }

    fun test_allocate_whenTwoProducts_shouldReturnWeightedResult() {
        // given
        val pkgName = "e2e.test_allocate_whenTwoProducts_shouldReturnWeightedResult"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out allocate 20 percent
                    1 kg otherOut allocate 80 percent
                }
                inputs {
                    1 m3 water
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        // when
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val system = Evaluator(symbolTable).eval(entryPoint)
        val assessment = Assessment(system)
        // then
        when (val result = assessment.inventory()) {
            // then
            is InventoryError -> fail("$result")
            is InventoryMatrix -> {
                val output1 = result.observablePorts.getElements()[0]
                val output2 = result.observablePorts.getElements()[1]
                val input = result.controllablePorts.getElements().first()
                val cf1 = result.value(output1, input)
                val cf2 = result.value(output2, input)

                val delta = 1E-9
                val expected1 = 1.0 * 20 / 100
                val expected2 = 1.0 * 80 / 100
                assertEquals(expected1, cf1.input.quantity().amount, delta)
                assertEquals(expected2, cf2.input.quantity().amount, delta)
            }
        }
    }

    fun test_unitAlias_whenInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenInfiniteLoop_shouldThrowAnError"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            unit foo {
                symbol = "foo"
                alias_for = 1 foo
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val evaluator = Evaluator(symbolTable)

        // when + then
        assertFailsWith(EvaluatorException::class, "Recursive dependency for unit foo", { evaluator.eval(entryPoint) })
    }

    fun test_unitAlias_whenNestedInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenNestedInfiniteLoop_shouldThrowAnError"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 foo
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val evaluator = Evaluator(symbolTable)

        // when + then
        assertFailsWith(EvaluatorException::class, "Recursive dependency for unit foo", { evaluator.eval(entryPoint) })
    }

    fun test_unitAlias_shouldNotThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_shouldNotThrowAnError"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 kg
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!
        val evaluator = Evaluator(symbolTable)

        // when, then does not throw
        evaluator.eval(entryPoint)
    }

    fun test_unitAlias_whenAdditionInAliasForField_shouldNotThrowAnError() {
        // given
        val pkgName = "e2e.test_unitAlias_whenAdditionInAliasForField_shouldNotThrowAnError"
        val vf = myFixture.createFile(
                "$pkgName.lca", """
            package $pkgName
            
            unit bar {
                symbol = "bar"
                alias_for = 1 kg
            }
            
            unit foo {
                symbol = "foo"
                alias_for = 1 bar + 1 bar
            }
            
            process p {
                products {
                    1 foo carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = symbolTable.processTemplates["p"]!!

        // when/then
        Evaluator(symbolTable).eval(entryPoint)
    }
}
