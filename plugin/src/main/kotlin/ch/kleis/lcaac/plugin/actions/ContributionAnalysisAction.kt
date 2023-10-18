package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.actions.tasks.ContributionAnalysisTask
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.ContributionAnalysisWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
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

    private fun displayInventory(
        project: Project,
        analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
        comparator: Comparator<MatrixColumnIndex<BasicNumber>>
    ) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val assessResultContent = ContributionAnalysisWindow(analysis, comparator, project, processName).getContent()
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


