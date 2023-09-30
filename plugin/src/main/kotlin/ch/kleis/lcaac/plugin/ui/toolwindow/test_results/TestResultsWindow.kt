package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel

class TestResultsWindow(
    val results: List<LcaTestResult>
) : LcaToolWindowContent {
    private val content: JPanel

    init {
        val tree = Tree(TestResultTree(results))
        content = JPanel(BorderLayout())
        content.add(tree)
        content.updateUI()
    }

    override fun getContent(): JPanel = content
}
