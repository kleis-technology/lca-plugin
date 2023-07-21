package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.*
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SankeyGraphBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    private data class SankeyRequiredInformation(
            val observedPort: MatrixColumnIndex,
            val allocatedSystem: SystemValue,
            val inventory: Inventory,
            val comparator: Comparator<MatrixColumnIndex>
    )

    private fun getRequiredInformation(@Suppress("SameParameterValue") process: String, vf: VirtualFile): SankeyRequiredInformation {
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file))
        val symbolTable = parser.load()
        val entryPoint = EProcessTemplateApplication(symbolTable.getTemplate(process)!!, emptyMap())
        val trace = Evaluator(symbolTable).trace(entryPoint)
        val assessment = Assessment(trace.getSystemValue(), trace.getEntryPoint())
        val inventory = assessment.inventory()
        val allocatedSystem = assessment.allocatedSystem
        val sankeyPort = inventory.getControllablePorts().getElements().first()
        return SankeyRequiredInformation(sankeyPort, allocatedSystem, inventory, trace.getProductOrder())
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("[Emission] my_substance(air)", "my_substance"),
                GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
                GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenRessource_thenSankey() {
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("[Resource] my_substance(air)", "my_substance"),
                GraphNode("my_product from p{}{}", "my_product")
        ).addLink(
                GraphLink("my_product from p{}{}", "[Resource] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("my_input", "my_input"),
                GraphNode("my_product from p{}{}", "my_product"),
        ).addLink(
                GraphLink("my_product from p{}{}", "my_input", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("climate_change", "climate_change"),
                GraphNode("my_product from p{}{}", "my_product"),
                GraphNode("[Emission] my_substance(air)", "my_substance")
        ).addLink(
                GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
                GraphLink("[Emission] my_substance(air)", "climate_change", 1.0)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)

    }

    @Test
    fun test_units() {
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("my_product from p{}{}", "my_product"),
                GraphNode("my_input from input{}{}", "my_input"),
                GraphNode("my_indicator", "my_indicator"),
        ).addLink(
                GraphLink("my_product from p{}{}", "my_input from input{}{}", 500.0),
                GraphLink("my_input from input{}{}", "my_indicator", 500.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("[Emission] my_substance(air)", "my_substance"),
                GraphNode("my_product from p{}{}", "my_product"),
                GraphNode("my_other_product from p{}{}", "my_other_product"),
        ).addLink(
                GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 1.0),
                GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 1.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

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
                GraphLink("my_product from p{}{}", "my_left_product from q{}{}", 1.0),
                GraphLink("my_product from p{}{}", "my_right_product from r{}{}", 1.0),
                GraphLink("my_left_product from q{}{}", "my_input from input{}{}", 1.0),
                GraphLink("my_right_product from r{}{}", "my_input from input{}{}", 1.0),
                GraphLink("my_input from input{}{}", "my_substance", 2.0),
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenComplexGraph_thenSankey() {
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
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

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
                GraphLink("my_product from p{}{}", "my_input from q{}{}", 1.5),
                GraphLink("my_other_product from p{}{}", "my_input from q{}{}", 0.5),
                GraphLink("my_input from q{}{}", "[Emission] my_substance(air)", 2.0),
                GraphLink("my_product from p{}{}", "[Emission] my_substance(air)", 0.75),
                GraphLink("my_other_product from p{}{}", "[Emission] my_substance(air)", 0.25),
                GraphLink("[Emission] my_substance(air)", "climate_change", 3.0)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenCycle_thenCorrectOrder() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
                "$pkgName.lca", """
                    process p1 {
                        products {
                            1 kg A
                        }
                        inputs {
                            1 kg B
                        }
                    }
                    process p2 {
                        products {
                            1 kg B
                        }
                        inputs {
                            0.1 kg A
                        }
                        emissions {
                            1 kg C
                        }
                    }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p1", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("A from p1{}{}", "A"),
                GraphNode("B from p2{}{}", "B"),
                GraphNode("C", "C"),
                GraphNode("cycle back to A", "cycle back to A")
        ).addLink(
                GraphLink("A from p1{}{}", "B from p2{}{}", 1.234567901234568),
                GraphLink("B from p2{}{}", "C", 1.1111111111111112),
                GraphLink("B from p2{}{}", "cycle back to A", 0.12345679012345681)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

    @Test
    fun test_whenMultiProductCycle_thenCorrectOrder() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
                "$pkgName.lca", """
                    process p1 {
                        products {
                            1 kg A allocate 50 percent
                            2 kg Aprime allocate 50 percent
                        }
                        inputs {
                            1 kg B
                        }
                    }
                    process p2 {
                        products {
                            1 kg B
                        }
                        inputs {
                            0.1 kg A
                        }
                        emissions {
                            1 kg C
                        }
                    }
                """.trimIndent())
        val (sankeyPort, allocatedSystem, inventory, comparator) = getRequiredInformation("p1", vf)
        val sut = SankeyGraphBuilder(allocatedSystem, inventory, comparator)

        // when
        val graph = sut.buildContributionGraph(sankeyPort)

        // then
        val expected = Graph.empty().addNode(
                GraphNode("A from p1{}{}", "A"),
                GraphNode("Aprime from p1{}{}", "Aprime"),
                GraphNode("B from p2{}{}", "B"), GraphNode("C", "C"),
                GraphNode("cycle back to A", "cycle back to A")
        ).addLink(
                GraphLink("A from p1{}{}", "B from p2{}{}", 0.5817174515235457),
                GraphLink("Aprime from p1{}{}", "B from p2{}{}", 0.5263157894736842),
                GraphLink("B from p2{}{}", "C", 1.0526315789473686),
                GraphLink("B from p2{}{}", "cycle back to A", 0.0554016620498615)
        )
        assertEquals(expected.nodes, graph.nodes)
        assertEquals(expected.links, graph.links)
    }

}