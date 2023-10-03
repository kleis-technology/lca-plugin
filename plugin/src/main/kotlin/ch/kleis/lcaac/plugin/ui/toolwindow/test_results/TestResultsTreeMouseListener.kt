package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.psi.LcaRangeAssertion
import com.intellij.pom.Navigatable
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.TreePath

object TestResultsTreeMouseListener : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent?) {
        val event = e ?: return
        if (event.clickCount < 2) { return }
        val source = event.source
        if (source !is Tree) { return }
        val path = source.selectionPath ?: return
        val target = path.lastPathComponent
        if (target !is AssertionResultEntry) { return }
        val testResultEntry = testResultEntry(path)
        val element = rangeAssertion(testResultEntry, target.id)
        navigateTo(element)
    }

    private fun testResultEntry(path: TreePath): TestResultEntry = path.parentPath.lastPathComponent as TestResultEntry
    private fun rangeAssertion(testResultEntry: TestResultEntry, id: Int): LcaRangeAssertion =
        testResultEntry.result.source.assertList.flatMap { it.rangeAssertionList }[id]

    private fun navigateTo(element: LcaRangeAssertion) {
        if (element is Navigatable && element.canNavigate()) {
            element.navigate(true)
        }
    }
}
