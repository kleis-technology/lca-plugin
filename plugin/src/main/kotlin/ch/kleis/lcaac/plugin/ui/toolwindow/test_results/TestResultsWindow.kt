package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel

class TestResultsWindow(
    val results: List<LcaTestResult>
) : LcaToolWindowContent {
    private val content: JPanel = JPanel(BorderLayout())

    companion object {
        private val LOG = Logger.getInstance(TestResultsWindow::class.java)
    }

    init {
        if (results.isNotEmpty()) {
            val model = TestResultTree(results)
            val tree = Tree(model)
            tree.addMouseListener(TestResultsTreeMouseListener)
            content.add(tree)
        } else {
            content.add(JBLabel("No tests found"), BorderLayout.PAGE_START)
        }
        content.updateUI()
    }

    override fun getContent(): JPanel = content
}
