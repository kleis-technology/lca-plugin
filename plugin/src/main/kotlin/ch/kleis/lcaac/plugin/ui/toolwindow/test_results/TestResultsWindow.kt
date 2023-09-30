package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.LcaTestResult
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.pom.Navigatable
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
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
        tree.addMouseListener(object: MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                LOG.warn("mouse clicked")
                if ((e?.clickCount ?: 0) < 2) {
                    return
                }
                val source = e?.source ?: return
                if (source !is Tree) {
                    LOG.warn("source is not a tree")
                    return
                }
                val path = source.selectionPath ?: return
                val last = path.lastPathComponent
                if (last !is AssertionResultEntry) {
                    LOG.warn("last = $last")
                    return
                }
                val id = last.id
                val parent = path.parentPath.lastPathComponent as TestResultEntry
                val element = parent.result.source
                    .assertList.flatMap { it.rangeAssertionList }[id]
                if (element != null
                    && element.navigationElement is Navigatable) {
                    val nav = element.navigationElement as Navigatable
                    if (nav.canNavigate()) {
                        LOG.warn("navigating to ${parent.result.name}[$id]")
                        nav.navigate(true)
                    }
                }
            }

            override fun mousePressed(e: MouseEvent?) {
            }

            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseExited(e: MouseEvent?) {
            }

        })
        content = JPanel(BorderLayout())
        content.add(tree)
        content.updateUI()
    }

    override fun getContent(): JPanel = content
}
