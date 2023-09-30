package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.LcaTestResult
import com.intellij.ui.tree.BaseTreeModel

class TestResultTree(
    results: List<LcaTestResult>
) : BaseTreeModel<TestResultTreeEntry>() {
    private val root: RootEntry
    private val entries: ArrayList<TestResultEntry>

    init {
        entries = ArrayList(results.map { result ->
            TestResultEntry(
                result = result,
                children = ArrayList(result.results.mapIndexed { id, it -> AssertionResultEntry(id, it) })
            )
        })
        root = RootEntry(
            children = entries
        )
    }

    override fun getRoot(): Any {
        return root
    }

    override fun getChildren(parent: Any?): MutableList<out TestResultTreeEntry> {
        return when (parent) {
            is RootEntry -> parent.children
            is TestResultEntry -> parent.children
            else -> ArrayList()
        }
    }
}
