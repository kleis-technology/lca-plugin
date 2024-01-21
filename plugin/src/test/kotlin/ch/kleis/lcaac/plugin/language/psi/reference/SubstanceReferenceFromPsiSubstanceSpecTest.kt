package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaac.plugin.psi.LcaTerminalBioExchange
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SubstanceReferenceFromPsiSubstanceSpecTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_resolve_whenNoSubCompartment_shouldDefaultToMatchingCompartment() {
        // given
        val pkgName =
            "language.psi.reference.subst.test_resolve_whenNoSubCompartment_shouldDefaultToMatchingCompartment"
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2(compartment="air", sub_compartment="nothing")
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first().terminalBioExchange!!
        val substanceSpec = element
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference?.resolve()

        // then
        val expected = SubstanceKeyIndex.Util.findSubstances(
            project,
            "$pkgName.co2_air.co2",
            "Emission",
            "air"
        ).first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_resolve_whenIncompatibleTypes_ShouldNotResolve() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    resources {
                        1 kg co2(compartment="air", sub_compartment="nothing")
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getResources().first().terminalBioExchange!!
        val substanceSpec = element
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference?.resolve()

        // then
        assertNull(actual)
    }

    @Test
    fun test_resolve__whenExact() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2(compartment="air")
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first().terminalBioExchange!!
        val substanceSpec = element
            .getSubstanceSpec()

        // when
        val actual = substanceSpec.reference?.resolve()

        // then
        val expected = SubstanceKeyIndex.Util.findSubstances(
            project,
            "$pkgName.co2_air.co2",
            "Emission",
            "air"
        ).first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.co2_air.lca", """
                package $pkgName.co2_air
               
                substance co2_air {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    reference_unit = kg
                }

                substance another_co2_air {
                    name = "co2"
                    type = Emission
                    compartment = "air"
                    sub_compartment = "another"
                    reference_unit = kg
                }
            """.trimIndent()
        )
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName

                import $pkgName.co2_air

                process p {
                    products {
                        1 kg a
                    }
                    emissions {
                        1 kg co2_air(compartment="air")
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getEmissions().first().terminalBioExchange!!
        val spec = element
            .getSubstanceSpec()

        // when
        val actual = spec.reference
            ?.variants?.map { (it as LookupElementBuilder).lookupString }
            ?.sorted() ?: emptyList()

        // then
        val expected = listOf(
            """co2_air(compartment="air")""",
            """another_co2_air(compartment="air", sub_compartment="another")""",
        ).sorted()
        TestCase.assertEquals(expected, actual)
    }
}
