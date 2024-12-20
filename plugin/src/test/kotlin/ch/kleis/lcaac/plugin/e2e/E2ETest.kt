package ch.kleis.lcaac.plugin.e2e

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ETechnoBlockEntry
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.actions.csv.CsvProcessor
import ch.kleis.lcaac.plugin.actions.csv.CsvRequest
import ch.kleis.lcaac.plugin.fixture.UnitFixture
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class E2ETest : BasePlatformTestCase() {
    private val ops = BasicOperations
    private val umap = with(ToValue(BasicOperations)) {
        Prelude.unitMap<BasicNumber>().map { it.value.toUnitValue() }
            .associateBy { it.symbol.toString() }
    }

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_shouldHandleKnowledgeCorrectly() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process a_proc {
                    products {
                        1 kg a
                    }
                    inputs {
                        1 kg b from b_proc
                        1 l c from c_proc
                    }
                }
                
                process b_proc {
                    products {
                        1 kg b
                    }
                    impacts {
                        1 kg gwp
                    }
                }
                
                process c_proc {
                    products {
                        1 l c
                    }
                    inputs {
                        1kg b from b_proc
                    }
                    impacts {
                        1 kg gwp
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)

        // when
        val template = symbolTable.getTemplate("a_proc")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("a from a_proc{}{}")
        val input = result.controllablePorts.get("gwp")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(3.0), umap["kg"]!! / umap["kg"]!!),
            cf,
        )
    }

    @Test
    fun test_whenReferencingBuiltinUnits_shouldLoadPreludeUnits() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                unit foo {
                    symbol = "foo"
                    alias_for = 1 kg
                }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val collector = LcaFileCollector(project)
        val files = collector.collect(file)
        val parser = LcaLoader(
            files,
            ops,
        )


        // when
        val symbolTable = parser.load()

        // then
        Prelude.unitMap<BasicNumber>().forEach {
            assertNotNull(symbolTable.getData(it.key))
        }
    }


    @Test
    fun test_patternMatching() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production match (geo = "FR")
                    }
                }
                
                process water_production {
                    labels {
                        geo = "UK"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }

                process water_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        10 kg co2
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)

        // when
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("carrot from p{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(10.0), umap["kg"]!! / umap["kg"]!!),
            cf,
        )
    }

    @Test
    fun test_patternMatching_withIndirection() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 kg intermediate from q(geo = "FR")
                    }
                }
                
                process q {
                    params {
                        geo = "UK"
                    }
                    products {
                        1 kg intermediate
                    }
                    inputs {
                        1 l water from water_production match (geo = geo)
                    }
                }
                
                process water_production {
                    labels {
                        geo = "UK"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }

                process water_production {
                    labels {
                        geo = "FR"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        10 kg co2
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)

        // when
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("carrot from p{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(10.0), umap["kg"]!! / umap["kg"]!!),
            cf,
        )
    }

    @Test
    fun test_stringArgumentIndirect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    variables {
                        geo = "FR"
                    }
                    inputs {
                        1 l water from water_production(geo = geo)
                    }
                }

                process water_production {
                    params {
                        geo = "GLO"
                    }
                    products {
                        1 l water
                    }
                    emissions {
                        1 kg co2
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)

        // when/then does not throw
        symbolTable.getTemplate("p")
            ?.let { Evaluator(symbolTable, ops, mockk()).trace(it) }!!
    }

    @Test
    fun test_stringArgument() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot
                    }
                    inputs {
                        1 l water from water_production(geo = "FR")
                    }
                }

                process water_production {
                    params {
                        geo = "GLO"
                    }
                    products {
                        1 l water
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)

        // when/then does not throw
        symbolTable.getTemplate("p")
            ?.let { Evaluator(symbolTable, ops, mockk()).trace(it) }!!
    }

    @Test
    fun test_csvProcessor() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        a = 0 kg
                        b = 0 kg
                        c = 1 kg
                    }
                    products {
                        1 kg out
                    }
                    inputs {
                        a + b + c in_prod
                    }
                }
            """.trimIndent()
        )
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val symbolTable = createFilesAndSymbols(vf)
        val project = mockk<Project>()
        every { project.basePath } returns "working_directory"
        val processor = CsvProcessor(project, symbolTable) { LcaacConfig() }
        val request = CsvRequest(
            "p",
            emptyMap(),
            mapOf("geo" to 0, "id" to 1, "a" to 2, "b" to 2),
            listOf("UK", "s00", "1.0", "1.0"),
        )

        // when
        val actual = processor.process(request)

        // then
        TestCase.assertEquals(1, actual.size)
        assertEquals(request, actual[0].request)
        val out = ProductValue(
            "out", kg,
            FromProcessRefValue(
                name = "p",
                arguments = mapOf(
                    "a" to QuantityValue(BasicNumber(1.0), kg),
                    "b" to QuantityValue(BasicNumber(1.0), kg),
                    "c" to QuantityValue(BasicNumber(1.0), kg),
                )
            )
        )
        assertEquals(
            out, actual[0].output
        )
        val key = ProductValue(
            "in_prod", kg,
        )
        assertEquals(QuantityValue(BasicNumber(3.0), kg), actual[0].impacts[key])
    }

    @Test
    fun test_exponentiationPriority() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                variables {
                    x = 10 m^2
                }

                process p {
                    products {
                        1 kg foo
                    }
                    inputs {
                        2 x^2 bar
                    }
                }
            """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)
        val reducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, mockk())
        val block = symbolTable.getTemplate("p")!!.body.inputs.first() as ETechnoBlockEntry<BasicNumber>
        val expr = block.entry.quantity

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected =
            EQuantityScale(
                ops.pure(200.0),
                EUnitLiteral(UnitSymbol.of("m").pow(4.0), 1.0, Dimension.of("length").pow(4.0))
            )
        TestCase.assertEquals(expected, actual)
    }


    @Test
    fun test_substanceResolution() {
        val pkgName = {}.javaClass.enclosingMethod.name
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

        // when
        val symbolTable = createFilesAndSymbols(vf)

        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.getElements().first()
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(QuantityValue(ops.pure(1.0), UnitValue.none()), cf)
    }

    @Test
    fun test_meta_whenKeywordAsKey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        assertEquals("unit", actual[0].getName())
        assertEquals("a", actual[0].getValue())
        assertEquals("process", actual[1].getName())
        assertEquals("b", actual[1].getValue())
    }

    @Test
    fun test_operationPriority() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
                    3 kg * q in_prod
                }
            }
        """.trimIndent()
        )
        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.getElements().first()
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(
                ops.pure(6.0),
                umap["m"]!! / umap["kg"]!!
            ),
            cf,
        )
    }

    @Test
    fun test_operationPriority_addVsMul() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                q_hi = 12 kg / km
                q_lo = 5 kg / km
                d = 10 km
                payload_hi = 1000 kg
                payload_lo = 100 kg
                coefficient = ( q_hi - q_lo ) / ( payload_hi - payload_lo )
                payload = 500 kg
                q = d * ( coefficient * ( payload - payload_lo ) + q_lo )
            }
        """.trimIndent()
        )

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val target = symbolTable.getData("q")!!
        val reducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, ops, mockk())
        val actual = with(ToValue(ops)) {
            reducer.reduce(target).toValue()
        } as QuantityValue<BasicNumber>

        // then
        assertEquals(
            actual.unit.dimension,
            Dimension.of("mass"),
        )
    }

    @Test
    fun test_twoInstancesSameTemplate_whenOneImplicit() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("office")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(3.0), umap["kg"]!! / umap["piece"]!!),
            cf,
        )
    }

    @Test
    fun test_twoInstancesSameTemplate_whenExplicit() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("office")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(3.0), umap["kg"]!! / umap["piece"]!!),
            cf,
        )
    }

    @Test
    fun test_manyInstancesSameTemplate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("office")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.get("office from office{}{}")
        val input = result.controllablePorts.get("co2")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals(
            QuantityValue(ops.pure(13.0), umap["kg"]!! / umap["piece"]!!),
            cf,
        )
    }

    @Test
    fun test_allocate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out1 allocate 90 percent
                    1 kg out2 allocate 10 percent
                }
                inputs {
                    1 kg in_prod
                }
            }
        """.trimIndent()
        )

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())
        val result = assessment.run().getImpactFactors()
        val output1 = result.observablePorts.getElements()[0]
        val output2 = result.observablePorts.getElements()[1]
        val input = result.controllablePorts.getElements().first()
        val cf1 = result.characterizationFactor(output1, input)
        val cf2 = result.characterizationFactor(output2, input)

        // then
        val delta = 1E-9
        assertEquals(0.9, cf1.amount.value, delta)
        assertEquals(0.1, cf2.amount.value, delta)
    }

    @Test
    fun test_allocate_whenOneProduct_allocateIsOptional() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        // when
        val symbolTable = createFilesAndSymbols(vf)
        val actual = ((symbolTable.getTemplate("p") as EProcessTemplate).body).products[0].allocation

        // then
        TestCase.assertNull(actual)
    }

    @Test
    fun test_allocate_whenSecondaryBlock_EmptyBlockIsAllowed() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        // when
        val symbolTable = createFilesAndSymbols(vf)
        val actual = ((symbolTable.getTemplate("p") as EProcessTemplate).body).products[0].allocation
        // then
        TestCase.assertNull(actual)
    }

    @Test
    fun test_allocate_whenTwoProducts_shouldReturnWeightedResult() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())

        // then
        val result = assessment.run().getImpactFactors()
        val output1 = result.observablePorts.getElements()[0]
        val output2 = result.observablePorts.getElements()[1]
        val input = result.controllablePorts.getElements().first()
        val cf1 = result.characterizationFactor(output1, input)
        val cf2 = result.characterizationFactor(output2, input)

        // then
        val delta = 1E-9
        val expected1 = 1.0 * 20 / 100
        val expected2 = 1.0 * 80 / 100
        assertEquals(expected1, cf1.amount.value, delta)
        assertEquals(expected2, cf2.amount.value, delta)
    }

    @Test
    fun test_unitAlias_whenInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val evaluator = Evaluator(symbolTable, ops, mockk())

        // when + then
        val e = assertFailsWith(EvaluatorException::class, null) { evaluator.trace(template) }
        assertEquals("Recursive dependency for unit foo", e.message)
    }

    @Test
    fun test_unitAlias_whenNestedInfiniteLoop_shouldThrowAnError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val evaluator = Evaluator(symbolTable, ops, mockk())

        // when + then
        val e = assertFailsWith(EvaluatorException::class, null) { evaluator.trace(template) }
        assertEquals("Recursive dependency for unit foo", e.message)
    }

    @Test
    fun test_unitAlias_shouldNotThrowAnError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val evaluator = Evaluator(symbolTable, ops, mockk())

        // when, then does not throw
        evaluator.trace(template)
    }

    @Test
    fun test_unitAlias_whenAdditionInAliasForField_shouldNotThrowAnError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
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
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!

        // when/then
        Evaluator(symbolTable, ops, mockk()).trace(template)
    }

    @Test
    fun test_processImpact_whenImpactBlockInProcess_shouldEvaluate() {
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                products {
                    1 kg out
                }
                impacts {
                    1 u climate_change
                }
            }
        """.trimIndent()
        )

        // when
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p")!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val system = trace.getSystemValue()
        val assessment = ContributionAnalysisProgram(system, trace.getEntryPoint())

        // then
        val result = assessment.run().getImpactFactors()
        val output = result.observablePorts.getElements().first()
        val input = result.controllablePorts.get("climate_change")
        val cf = result.characterizationFactor(output, input)

        // then
        assertEquals("climate_change", input.getDisplayName())
        assertEquals(
            QuantityValue(ops.pure(1.0), umap["u"]!! / umap["kg"]!!),
            cf,
        )
    }

    @Test
    fun test_processInput_whenWrongUnit_thenShouldThrow() {
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p1 {
                products {
                    1 kg out
                }
                inputs {
                    1 l in_prod
                }
            }
            process p2 {
                products {
                    1 kg in_prod
                }
            }
        """.trimIndent()
        )
        val symbolTable = createFilesAndSymbols(vf)
        val template = symbolTable.getTemplate("p1")!!

        // when/then
        val e = assertFailsWith(
            EvaluatorException::class,
        ) {
            Evaluator(symbolTable, ops, mockk()).trace(template)
        }
        assertEquals("incompatible dimensions: length³ vs mass for product in_prod", e.message)
    }

    private fun createFilesAndSymbols(vf: VirtualFile): SymbolTable<BasicNumber> {
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLoader(sequenceOf(UnitFixture.getInternalUnitFile(myFixture), file), ops)
        return parser.load()
    }
}
