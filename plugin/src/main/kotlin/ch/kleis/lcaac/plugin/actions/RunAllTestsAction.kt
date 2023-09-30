package ch.kleis.lcaac.plugin.actions

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
    }
}
