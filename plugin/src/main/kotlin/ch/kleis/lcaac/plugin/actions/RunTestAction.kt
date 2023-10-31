package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.RunAllTestsTask
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager

class RunTestAction(
    private val testName: String,
) : AnAction(
    "Run",
    "Run",
    AllIcons.Actions.Execute,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val task = RunAllTestsTask(project, RunAllTestsAction::onTestSuccess) {
            runReadAction {
                file.findTest(testName)
                    ?.let { listOf(it) }
                    ?: emptyList()
            }
        }
        ProgressManager.getInstance().run(task)
    }
}
