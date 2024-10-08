package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStubKeyIndex
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import ch.kleis.lcaac.plugin.psi.LcaScaleQuantityExpression
import ch.kleis.lcaac.plugin.psi.LcaSliceExpression
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataReferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_resolve_whenFromForEach() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            process p {
                inputs {
                    for_each row from source {
                        variables {
                            x = row.x
                        }
                    }
                }
            }
        """.trimIndent())
        val process = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val blockForEach = process.blockInputsList.first()
            .technoInputExchangeList.first()
            .technoBlockForEach!!
        val slice = blockForEach
            .getVariablesList().first()
            .assignmentList.first()
            .dataExpressionList[1] as LcaSliceExpression
        val ref = slice.dataRef

        // when
        val actual = ref.reference.resolve()

        // then
        assertEquals(blockForEach, actual)
    }

    @Test
    fun test_resolve_whenFromGlobalAssignment() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
           package $pkgName

           variables {
               x = 1 kg
           }

           process a {
               products {
                   x carrot
               }
           }
        """.trimIndent()
        )
        val fqn = "$pkgName.a"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val ref = process.getProducts().first()
            .dataExpression as LcaDataRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = GlobalAssigmentStubKeyIndex
            .findGlobalAssignments(project, "$pkgName.x").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenFromUnitDefinition() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName

            unit x {
               symbol = "x"
               dimension = "x"
            }

            process a {
               products {
                   3 x carrot
               }
            }
        """.trimIndent())
        val fqn = "$pkgName.a"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val assignment = process
            .getProducts().first()
            .dataExpression as LcaScaleQuantityExpression
        val ref = assignment.dataExpression!! as LcaDataRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = UnitStubKeyIndex.findUnits(project, "$pkgName.x").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenFromProcessParameter() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile("$pkgName.lca", """
            package $pkgName

            process caller {
                products {
                    1 kg salad
                }
                inputs {
                    1 kg carrot from called(x = 2 kg)
                }
            }

            process called {
                params {
                    x = 1 kg
                }
                products {
                    1 kg carrot
                }
            }
        """.trimIndent())
        val fqn = "$pkgName.caller"
        val process = ProcessStubKeyIndex.findProcesses(project, fqn).first()
        val element = process
            .getInputs().first().terminalTechnoInputExchange!!
        val ref = element
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .argumentList.first()
            .parameterRef

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = ProcessStubKeyIndex.findProcesses(project, "$pkgName.called").first()
            .getParamsList().first()
            .assignmentList.first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenInSubstanceReferenceUnitField() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            unit foo {
                symbol = "foo"
                dimension = "foo"
            }

            unit bar {
                symbol = "bar"
                dimension = "bar"
            }

            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = foo
            }
        """.trimIndent())
        val ref = SubstanceKeyIndex.Util.findSubstances(
            project,
            "$pkgName.s",
            "Resource",
            "c"
        ).first()
            .getReferenceUnitField()
            .dataExpression

        // when
        val actual = ref.reference!!.resolve()

        // then
        val expected = UnitStubKeyIndex.findUnits(project, "$pkgName.foo").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants_whenInSubstanceReferenceField() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile("$pkgName.lca", """
            package $pkgName
            
            unit foo {
                symbol = "foo"
                dimension = "foo"
            }

            unit bar {
                symbol = "bar"
                dimension = "bar"
            }

            substance s {
                name = "s"
                type = Resource
                compartment = "c"
                reference_unit = foo
            }
        """.trimIndent())
        val ref = SubstanceKeyIndex.Util.findSubstances(
            project,
            "$pkgName.s",
            "Resource",
            "c"
        ).first()
            .getReferenceUnitField()
            .dataExpression

        // when
        val actual = ref.reference!!.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        assertContainsElements(actual, "foo", "bar")
    }
}
