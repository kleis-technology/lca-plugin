package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.plugin.actions.tasks.ContributionAnalysisTask
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.ContributionAnalysisWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessProcessAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    "Assess",
    "Assess",
    AllIcons.Actions.Execute,
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val task = ContributionAnalysisTask(project, processName, file, matchLabels, ::displayInventory)
        ProgressManager.getInstance().run(task)
    }

    private fun displayInventory(task: ContributionAnalysisTask) {
        val toolWindow = ToolWindowManager.getInstance(task.project).getToolWindow("LCA Output") ?: return
        val result = task.data!!
        val assessResultContent =
            ContributionAnalysisWindow(result.first, result.second, task.project, processName).getContent()
        val content = ContentFactory.getInstance().createContent(
            assessResultContent,
            "Contribution analysis of $processName",
            false,
        )
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

}


