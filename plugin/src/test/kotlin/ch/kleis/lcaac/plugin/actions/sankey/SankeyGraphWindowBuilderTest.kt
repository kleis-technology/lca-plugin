package ch.kleis.lcaac.plugin.actions.sankey

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.fixture.UnitFixture
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.openapi.ui.naturalSorted
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SankeyGraphWindowBuilderTest : BasePlatformTestCase() {
    private val ops = BasicOperations

    override fun getTestDataPath(): String {
        return "testdata"
    }

    private data class SankeyRequiredInformation(
        val observedPort: MatrixColumnIndex<BasicNumber>,
        val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
        val comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    )

    private fun getRequiredInformation(
        @Suppress("SameParameterValue") process: String,
        vf: VirtualFile
    ): SankeyRequiredInformation {
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLoader(sequenceOf(UnitFixture.getInternalUnitFile(myFixture), file), ops)
        val symbolTable = parser.load()
        val template = symbolTable.getTemplate(process)!!
        val trace = Evaluator(symbolTable, ops, mockk()).trace(template)
        val assessment = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        val analysis = assessment.run()
        val sankeyPort = analysis.getControllablePorts().getElements().first()
        return SankeyRequiredInformation(sankeyPort, analysis, trace.getComparator())
    }

    @Test
    fun test_weird() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                        process transport_truck {
                            params {
                                weight = 2 ton
                                ratio = 2 kg/ton
                                fuel = "diesel"
                            }
                            products {
                                1 ton*km truck
                            }
                            variables {
                                fuel_amount = weight * ratio
                            }
                            inputs {
                                fuel_amount fuel_emissions from combustion
                            }
                        }

                        process combustion {
                            products {
                                1 kg fuel_emissions
                            }
                            emissions {
                                0.3 kg co2
                            }
                        }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("transport_truck", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("co2", "co2"),
            GraphNode("truck from transport_truck{}{weight=2.0 ton, ratio=2.0 kg.ton⁻¹, fuel=diesel}", "truck"),
            GraphNode("fuel_emissions from combustion{}{}", "fuel_emissions"),
        ).addLink(
            GraphLink(
                "truck from transport_truck{}{weight=2.0 ton, ratio=2.0 kg.ton⁻¹, fuel=diesel}",
                "fuel_emissions from combustion{}{}",
                1.2,
                "1.2 kg"
            ),
            GraphLink("fuel_emissions from combustion{}{}", "co2", 1.2, "1.2 kg"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenEmission_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           emissions{
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0, "1 m3"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenResource_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           resources {
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Resource] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Resource] my_substance(air)", 1.0, "1 m3"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenProducts_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process p {
                    products {
                        1 kg my_product
                    }
                    inputs {
                        1 kg my_input
                    }
                }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_input", "my_input"),
            GraphNode("my_product from p{}{}", "my_product"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input", 1.0, "1 kg"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenTwoLinks_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                process p {
                    products {
                        1 kg prod
                    }
                    inputs {
                        2 kg qrod
                    }
                }
                process q {
                    products {
                        1 kg qrod
                    }
                    emissions {
                        2.5 kg my_emission
                    }
               }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("prod from p{}{}", "prod"),
            GraphNode("qrod from q{}{}", "qrod"),
            GraphNode("my_emission", "my_emission"),
        ).addLink(
            GraphLink("prod from p{}{}", "qrod from q{}{}", 5.0, "5 kg"),
            GraphLink("qrod from q{}{}", "my_emission", 5.0, "5 kg"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenSubstanceImpacts_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                substance my_substance {
                    name = "my_substance"
                    type = Emission
                    compartment = "air"
                    reference_unit = m3
                    impacts {
                        1 u climate_change
                    }
                }
                process p {
                    products {
                        1 kg my_product
                    }
                    emissions {
                        1 m3 my_substance (compartment = "air")
                    }
                }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("[Emission] my_substance(air)", "my_substance")
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0, "1 u"),
            GraphLink("[Emission] my_substance(air)", "climate_change", 1.0, "1 u")
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())

    }

    @Test
    fun `When processes define different units of the same dimension, normalization is applied when building graph links`() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product
                           }
                           inputs {
                               50 percent * 1 kg my_input
                           }
                       }
                       process input {
                           products {
                                1 kg my_input
                           }
                           inputs {
                               1000 g my_indicator
                           }
                      }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_input from input{}{}", "my_input"),
            GraphNode("my_indicator", "my_indicator"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input from input{}{}", 0.5, "5E-1 g"),
            GraphLink("my_input from input{}{}", "my_indicator", 0.5, "5E-1 g"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenAllocation_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       process p {
                           products {
                               1 kg my_product allocate 50 percent
                               1 kg my_other_product allocate 50 percent
                           }
                           emissions {
                               2 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("[Emission] my_substance(air)", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_other_product from p{}{}", "my_other_product"),
        ).addLink(
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0, "1 m3"),
            GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 1.0, "1 m3"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun test_whenDiamondShaped_thenSankey() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                    process p {
                        products {
                            1 kg my_product
                        }
                        inputs {
                            1 kg my_left_product
                            1 kg my_right_product
                        }
                    }
                    process q {
                        products {
                            1 kg my_left_product
                        }
                        inputs {
                            1 kg my_input
                        }
                    }
                    process r {
                        products {
                            1 kg my_right_product
                        }
                        inputs {
                            1 kg my_input
                        }
                    }
                    process input {
                        products {
                            1 kg my_input
                        }
                        emissions {
                            1 kg my_substance
                        }
                    }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("my_substance", "my_substance"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_left_product from q{}{}", "my_left_product"),
            GraphNode("my_right_product from r{}{}", "my_right_product"),
            GraphNode("my_input from input{}{}", "my_input"),
        ).addLink(
            GraphLink("my_product from p{}{}", "my_left_product from q{}{}", 1.0, "1 kg"),
            GraphLink("my_product from p{}{}", "my_right_product from r{}{}", 1.0, "1 kg"),
            GraphLink("my_left_product from q{}{}", "my_input from input{}{}", 1.0, "1 kg"),
            GraphLink("my_right_product from r{}{}", "my_input from input{}{}", 1.0, "1 kg"),
            GraphLink("my_input from input{}{}", "my_substance", 2.0, "2 kg"),
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }

    @Test
    fun `When provided with a semi-complex graph using allocation, substances and several processes, the graph is as expected`() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                       substance my_substance {
                           name = "my_substance"
                           type = Emission
                           compartment = "air"
                           reference_unit = m3
                           impacts {
                               1 u climate_change
                           }
                       }
                       
                       process p {
                           products {
                               1 kg my_product allocate 75 percent
                               1 kg my_other_product allocate 25 percent
                           }
                           inputs {
                               2 kg my_input
                           }
                           emissions {
                               1 m3 my_substance (compartment = "air")
                           }
                       }

                       process q {
                           products {
                               1 kg my_input
                           }
                           emissions {
                               1 m3 my_substance (compartment = "air")
                           }
                       }
                """.trimIndent()
        )
        val (sankeyPort, analysis, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(analysis, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
            GraphNode("climate_change", "climate_change"),
            GraphNode("my_input from q{}{}", "my_input"),
            GraphNode("my_product from p{}{}", "my_product"),
            GraphNode("my_other_product from p{}{}", "my_other_product"),
            GraphNode("[Emission] my_substance(air)", "my_substance")
        ).addLink(
            GraphLink("my_product from p{}{}", "my_input from q{}{}", 1.5, "1.5 u"),
            GraphLink("my_other_product from p{}{}", "my_input from q{}{}", 0.5, "5E-1 u"),
            GraphLink("my_input from q{}{}", "[Emission] my_substance(air)", 2.0, "2 u"),
            GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 0.75, "7.5E-1 u"),
            GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 0.25, "2.5E-1 u"),
            GraphLink("[Emission] my_substance(air)", "climate_change", 3.0, "3 u")
        )
        assertEquals(expected.nodes.naturalSorted(), graph.nodes.naturalSorted())
        assertEquals(expected.links.naturalSorted(), graph.links.naturalSorted())
    }
}
