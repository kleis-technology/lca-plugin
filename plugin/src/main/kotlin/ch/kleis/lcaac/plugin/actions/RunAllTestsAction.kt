package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.language.psi.stub.test.TestStubKeyIndex
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class RunAllTestsAction : AnAction(
    "Run All Tests",
    "Run all tests",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(RunAllTestsAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val tests = TestStubKeyIndex.findAllTests(project)
    }
}
