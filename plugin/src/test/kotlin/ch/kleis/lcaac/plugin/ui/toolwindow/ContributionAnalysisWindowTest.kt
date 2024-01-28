package ch.kleis.lcaac.plugin.ui.toolwindow

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.fixture.UnitFixture
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.ImpactAssessmentTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.InventoryTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.SupplyTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.CopyPastableTablePane
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.WithHeaderTransferableHandler
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBViewport
import com.intellij.ui.table.JBTable
import io.mockk.mockk
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.datatransfer.DataFlavor

@RunWith(JUnit4::class)
class ContributionAnalysisWindowTest : BasePlatformTestCase() {
    private val ops = BasicOperations

    @Test
    fun test_impactAssessmentPane_copyPaste() {
        // Given
        val pkgName = {}.javaClass.enclosingMethod.name
        val analysis = analysisFixture(pkgName)
        val tablePane = CopyPastableTablePane(
            ImpactAssessmentTableModel(
                analysis,
            ),
            project,
        ).content
        val scrollPane = tablePane.getComponent(0) as JBScrollPane
        val viewPort = scrollPane.getComponent(0) as JBViewport
        val table = viewPort.getComponent(0) as JBTable
        table.setRowSelectionInterval(0, 0)
        val sut = table.transferHandler as WithHeaderTransferableHandler

        // When
        val result = sut.createTransferable(table) as BasicTransferable

        // Then
        val html = result.getTransferData(DataFlavor("text/html;class=java.lang.String")) as String
        val expectedHtml = """
            |<html>
            |<body>
            |<table>
            |<tr>
            |  <th>indicator</th>
            |  <th>unit</th>
            |  <th>carrot</th>
            |</tr>
            |<tr>
            |  <td>gwp</td>
            |  <td>kg</td>
            |  <td>2.0</td>
            |</tr>
            |</table>
            |</body>
            |</html>
            """.trimMargin()
        assertEquals(expectedHtml, html)
        val text = result.getTransferData(DataFlavor("text/plain;class=java.lang.String")) as String
        val expectedText = """
            |indicator	unit	carrot	
            |gwp	kg	2.0
            """.trimMargin()
        assertEquals(expectedText, text)
    }

    @Test
    fun test_inventoryPane_copyPaste() {
        // Given
        val pkgName = {}.javaClass.enclosingMethod.name
        val analysis = analysisFixture(pkgName)
        val tablePane = CopyPastableTablePane(
            InventoryTableModel(
                analysis,
                Comparator.comparing { it.getUID() },
            ),
            project,
        ).content
        val scrollPane = tablePane.getComponent(0) as JBScrollPane
        val viewPort = scrollPane.getComponent(0) as JBViewport
        val table = viewPort.getComponent(0) as JBTable
        table.setRowSelectionInterval(0, 0)
        val sut = table.transferHandler as WithHeaderTransferableHandler

        // When
        val result = sut.createTransferable(table) as BasicTransferable

        // Then
        val html = result.getTransferData(DataFlavor("text/html;class=java.lang.String")) as String
        val expectedHtml = """
            |<html>
            |<body>
            |<table>
            |<tr>
            |  <th>type</th>
            |  <th>name</th>
            |  <th>compartment</th>
            |  <th>sub_compartment</th>
            |  <th>unit</th>
            |  <th>carrot</th>
            |</tr>
            |<tr>
            |  <td>Resource</td>
            |  <td>no2</td>
            |  <td>ground</td>
            |  <td>in soil</td>
            |  <td>kg</td>
            |  <td>1.0</td>
            |</tr>
            |</table>
            |</body>
            |</html>
            """.trimMargin()
        assertEquals(expectedHtml, html)
        val text = result.getTransferData(DataFlavor("text/plain;class=java.lang.String")) as String
        val expectedText = """
            |type	name	compartment	sub_compartment	unit	carrot	
            |Resource	no2	ground	in soil	kg	1.0
            """.trimMargin()
        assertEquals(expectedText, text)
    }

    @Test
    fun test_supplyPane_copyPaste() {
        // Given
        val pkgName = {}.javaClass.enclosingMethod.name
        val analysis = analysisFixture(pkgName)
        val tablePane = CopyPastableTablePane(
            SupplyTableModel(
                analysis,
                Comparator.comparing { it.getUID() },
            ),
            project,
        ).content
        val scrollPane = tablePane.getComponent(0) as JBScrollPane
        val viewPort = scrollPane.getComponent(0) as JBViewport
        val table = viewPort.getComponent(0) as JBTable
        table.setRowSelectionInterval(0, 0)
        val sut = table.transferHandler as WithHeaderTransferableHandler

        // When
        val result = sut.createTransferable(table) as BasicTransferable

        // Then
        val html = result.getTransferData(DataFlavor("text/html;class=java.lang.String")) as String
        val expectedHtml = """
            |<html>
            |<body>
            |<table>
            |<tr>
            |  <th>name</th>
            |  <th>process</th>
            |  <th>params</th>
            |  <th>labels</th>
            |  <th>unit</th>
            |  <th>carrot</th>
            |</tr>
            |<tr>
            |  <td>carrot</td>
            |  <td>p</td>
            |  <td></td>
            |  <td></td>
            |  <td>kg</td>
            |  <td>1.0</td>
            |</tr>
            |</table>
            |</body>
            |</html>
            """.trimMargin()
        assertEquals(expectedHtml, html)
        val text = result.getTransferData(DataFlavor("text/plain;class=java.lang.String")) as String
        val expectedText = """
            |name	process	params	labels	unit	carrot	
            |carrot	p			kg	1.0
            """.trimMargin()
        assertEquals(expectedText, text)
    }

    private fun analysisFixture(
        pkgName: String,
    ): ContributionAnalysis<BasicNumber, BasicMatrix> {
        val virtualFile = myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            import builtin_units
            
            process p {
                products {
                    1 kg carrot
                }
                inputs {
                    1 l water
                }
                resources {
                    1 kg no2(compartment = "ground", sub_compartment="in soil")
                }
            }
            
            process q {
                products {
                    1 l water
                }
                impacts {
                    1 kg gwp
                }
            }
            
            substance no2 {
                name = "no2"
                type = Resource
                compartment = "ground"
                sub_compartment = "in soil"
                reference_unit = kg

                impacts {
                    1 kg gwp
                }
            }
        """.trimIndent()
        )
        val lcaFile = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val builtinUnitsVirtualFile = myFixture.createFile(
            "$pkgName.units.lca", UnitFixture.basicUnits
        )
        val builtinUnitsLcaFile = PsiManager.getInstance(project).findFile(builtinUnitsVirtualFile) as LcaFile
        val loader = LcaLoader(sequenceOf(lcaFile, builtinUnitsLcaFile), ops)
        val symbolTable = loader.load()
        val template = symbolTable.getTemplate("p")!!
        val evaluator = Evaluator(symbolTable, ops, mockk())
        val trace = evaluator.trace(template)
        val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
        return program.run()
    }

    override fun getTestDataPath(): String {
        return "testdata"
    }
}
