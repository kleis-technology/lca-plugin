package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel

class TestResultsWindow(
    val results: List<LcaTestResult>
) : LcaToolWindowContent {
    private val content: JPanel

    companion object {
        private val LOG = Logger.getInstance(TestResultsWindow::class.java)
    }

    init {
        val model = TestResultTree(results)
        val tree = Tree(model)
        tree.addMouseListener(TestResultsTreeMouseListener)
        content = JPanel(BorderLayout())
        content.add(tree)
        content.updateUI()
    }

    override fun getContent(): JPanel = content
}
