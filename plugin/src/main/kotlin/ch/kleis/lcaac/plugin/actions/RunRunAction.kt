package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.RunTask
import ch.kleis.lcaac.plugin.psi.LcaRun
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager

class RunRunAction(
    private val run: LcaRun,
    private val runnerName: String,
) : AnAction(
    "Run",
    "Run",
    AllIcons.Actions.Execute,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val task = RunTask(project, run, runnerName)

        ProgressManager.getInstance().run(task)
    }
}
